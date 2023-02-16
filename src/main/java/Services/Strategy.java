package Services;

import Enums.*;
import Models.*;
import Services.BotService;

import java.util.*;
import java.util.stream.*;

public class Strategy {
    // Constant
    public static final Integer objectEnumSize = ObjectTypes.values().length;
    public static final Integer infinity = Integer.MAX_VALUE;

    public static GameObject bot;
    public static GameState gameState;
    public static World world;
    public static Random random = new Random();
    public static List<GameObject>[] objectList = new List[objectEnumSize+1]; // enum indexing same as Object Types
    public static List<GameObject> weakEnemy;
    public static List<GameObject> strongEnemy;
    public static long startTime;
    public static boolean supernovaFired = false;
    
    public static void compute(BotService service, PlayerAction playerAction){
        startTime = System.nanoTime();
        extractBotService(service);
        extractGameObject();

        defaultAction(playerAction);
        moveLogic(playerAction); // ! runFromBorder bad in late game
        activateAfterBurnerLogic(playerAction); // ! not finished
        deactivateAfterBurnerLogic(playerAction); // ! not finished
        fireTorpedoLogic(playerAction);
        fireTeleporterLogic(playerAction);
        teleportLogic(playerAction);
        shieldLogic(playerAction); // ! sometimes not open up
        fireSupernovaLogic(playerAction); // ! not finished
        detonateSupernovaLogic(playerAction); // ! not finished
        
        // debugBotInfo(playerAction, 1); // tick, execution time, bot info
        // debugBotInfo(playerAction, 2); // bot inventory
        // debugBotInfo(playerAction, 3); // enemy info
        // debugBotInfo(playerAction, 4); // supernova
        service.setPlayerAction(playerAction);
    }

    private static boolean notEmpty(List<GameObject> object){
        return object != null && !object.isEmpty();
    }

    private static boolean isEmpty(List<GameObject> object){
        return !notEmpty(object);
    }
    
    private static double getDistanceBetween(GameObject object1, GameObject object2) {
        return getDistanceBetween(object1.getPosition(), object2.getPosition());
    }

    private static double getDistanceBetween(Position object1, Position object2) {
        int dX = Math.abs(object1.x - object2.x);
        int dY = Math.abs(object1.y - object2.y);
        return Math.sqrt(dX * dX + dY * dY);
    }

    private static int getHeadingBetween(GameObject otherObject) {
        return getHeadingBetween(otherObject.getPosition());
    }

    private static int getHeadingBetween(Position otherObject) {
        int direction = toDegrees(
                                    Math.atan2(otherObject.y - bot.getPosition().y,
                                                otherObject.x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private static int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }
    
    public static void extractBotService(BotService service){
        bot = service.getBot();
        gameState = service.getGameState();
    }

    public static void extractGameObject(){
        world = gameState.getWorld();
        List<GameObject> gameObjectList = gameState.getGameObjects();
        List<GameObject> playerList = gameState.getPlayerGameObjects();

        if(isEmpty(gameObjectList) && isEmpty(playerList))return;
        // create array
        for(int i=1;i<=objectEnumSize;i++){
            objectList[i] = new ArrayList<GameObject>();
        }
        // filter
        for(GameObject gameObject : gameObjectList){
            int enumIdx = gameObject.getGameObjectType().getValue();
            objectList[enumIdx].add(gameObject);
        }
        for(GameObject gameObject : playerList){
            int enumIdx = gameObject.getGameObjectType().getValue();
            objectList[enumIdx].add(gameObject);
        }
        // sorting
        for(int i=1;i<=objectEnumSize;i++){
            objectList[i] = objectList[i].stream()
                                        .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item)))
                                        .collect(Collectors.toList());
        }

        // get strong and weak enemy
        weakEnemy = new ArrayList<GameObject>();
        strongEnemy = new ArrayList<GameObject>();
		int weakEnemySizeOffset = 20;
        if(isEmpty(objectList[1]))return;
        objectList[1].remove(0); // remove ourself from list
        for(GameObject player : objectList[1]){
            if(bot.size >= player.size + weakEnemySizeOffset){
                weakEnemy.add(player);
            }else{
                strongEnemy.add(player);
            }
        }
    }

    public static void defaultAction(PlayerAction playerAction){
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = random.nextInt(360);
    }

    public static void moveLogic(PlayerAction playerAction){
        List<List<GameObject>> towardObject = new ArrayList<List<GameObject>>();
        towardObject.add(objectList[2]); // food
        towardObject.add(objectList[7]); // super food
        towardObject.add(objectList[8]); // supernova pick
        towardObject.add(weakEnemy);

        List<List<GameObject>> avoidObject = new ArrayList<List<GameObject>>();
        avoidObject.add(objectList[4]); // gas
        // avoidObject.add(objectList[5]); // asteroid
        avoidObject.add(objectList[6]); // torpedo
        avoidObject.add(objectList[10]); // teleporter
        avoidObject.add(strongEnemy);

        // calc nearest dist
        List<Double> towardDist = new ArrayList<Double>();
        List<Double> avoidDist = new ArrayList<Double>();
        for(List<GameObject> objects : towardObject){
            double dist = getShortestObjectListDistance(objects);
            if(dist != infinity)dist -= objects.get(0).size + bot.size;
            towardDist.add(dist);
        }
        for(List<GameObject> objects : avoidObject){
            double dist = getShortestObjectListDistance(objects);
            if(dist != infinity)dist -= objects.get(0).size + bot.size;
            avoidDist.add(dist);
        }

        double targetDist = infinity;

        for(int i=0;i<towardDist.size();i++){
            if(towardDist.get(i) < targetDist){
                targetDist = towardDist.get(i);
                playerAction.heading = getHeadingBetween(towardObject.get(i).get(0));
            }
        }

        for(int i=0;i<avoidDist.size();i++){
            if(avoidDist.get(i) < targetDist){
                targetDist = avoidDist.get(i);
                playerAction.heading = (getHeadingBetween(avoidObject.get(i).get(0)) + 180) % 360;
            }
        }

        runFromBorder(playerAction);
    }

    public static double getShortestObjectListDistance(List<GameObject> list){
        return notEmpty(list) ? getDistanceBetween(bot, list.get(0)) : infinity;
    }

    public static void fireTorpedoLogic(PlayerAction playerAction){
        int minimumSize = 50;
        double distLowerBound = 600;
        int fireChance = random.nextInt(100);
        if(bot.TorpedoSalvoCount >= 3)fireChance = random.nextInt(40, 100); // maximum is 5, dont waste
        if(bot.TorpedoSalvoCount >= 2 && getShortestObjectListDistance(objectList[1])<100)fireChance = random.nextInt(75, 100);
        if(bot.size < minimumSize || fireChance < 97 || isEmpty(objectList[1]))return;
        for(GameObject player : objectList[1]){
            double dist = getDistanceBetween(bot, player) - player.size;
            if(dist >= distLowerBound){
                break;
            }
            playerAction.action = PlayerActions.FIRETORPEDOES;
            playerAction.heading = getHeadingBetween(player);
            break;
        }
    }

    public static void fireTeleporterLogic(PlayerAction playerAction){
        double minimumSize = 80;
        double enemyMinimumSize = 30; // eat very small enemy not worth it (?) at cost of firing teleport
        double bigSize = 170;
		int fireChance = random.nextInt(100);
        if (bot.size > bigSize) fireChance=random.nextInt(40,100);
        if(isEmpty(weakEnemy) || bot.TeleporterCount == 0 || bot.size < minimumSize 
            || notEmpty(objectList[10]) || fireChance < 97)return;
        double distLowerBound = 1200;
        double weakSizeMultiplier = 0.9;
        GameObject target = null;
        for(GameObject enemy : weakEnemy){
            if(enemy.size < enemyMinimumSize)continue;
            if(enemy.size > (bot.size - 20) * weakSizeMultiplier)continue;
            if(getDistanceBetween(bot, enemy) <= distLowerBound){
                target = enemy;
                break;
            }
        }
        if(target != null){
            playerAction.action = PlayerActions.FIRETELEPORT;
            playerAction.heading = getHeadingBetween(target);
        }
    }

    public static void teleportLogic(PlayerAction playerAction){
        if(isEmpty(objectList[10]))return;
        double distLowerBound = 25; 
        for(GameObject teleporter : objectList[10]){
            for(GameObject enemy : weakEnemy){
                double dist = getDistanceBetween(enemy, teleporter);
                dist -= enemy.size + bot.size;
                if(dist <= distLowerBound){
                    playerAction.action = PlayerActions.TELEPORT;
                    return;
                }
            }
        }
    }

    public static void shieldLogic(PlayerAction playerAction){
        int minimumSize = 50;
        if(bot.ShieldCount == 0 || bot.size < minimumSize || isEmpty(objectList[6]))return;
        double distLowerBound = 250; // big because delay (?)
        double toleratedAngle = 10;
        boolean useShield = false;
        double minimumTorpedoSize = 3;
        for(GameObject torpedo : objectList[6]){
            if(torpedo.size <= minimumTorpedoSize)continue;
            double dist = getDistanceBetween(bot, torpedo) - bot.size - torpedo.size;
            int angle1 = Math.abs(torpedo.currentHeading - ((getHeadingBetween(torpedo) + 180) % 360));
            if(dist > distLowerBound || angle1 > toleratedAngle)continue;
            useShield = true;
            break;
        }
        if(useShield)playerAction.action = PlayerActions.ACTIVATESHIELD;
    }

    public static void activateAfterBurnerLogic(PlayerAction playerAction){
            
    }

    public static void deactivateAfterBurnerLogic(PlayerAction playerAction){
        
    }

    public static void fireSupernovaLogic(PlayerAction playerAction){
        if(bot.SupernovaAvailable != 0){
            playerAction.heading = getHeadingBetween(objectList[1].get(objectList[1].size()-1)); // farthest player
            playerAction.action = PlayerActions.FIRESUPERNOVA;
        }
    }
    
    public static void detonateSupernovaLogic(PlayerAction playerAction){
        if(isEmpty(objectList[9]))return;
        double distLowerBound = 300;
        if(getShortestObjectListDistance(objectList[9]) > distLowerBound){
            playerAction.action = PlayerActions.DETONATESUPERNOVA;
            supernovaFired = true;
        }

    }

    public static void runFromBorder(PlayerAction playerAction){
        if(world == null || world.getRadius() == null)return;
        double distLowerBound = 20;
        if(world.getCurrentTick()!=null)distLowerBound+=world.getCurrentTick()/10;
        double dist = world.radius - getDistanceBetween(bot.getPosition(), world.centerPoint) - bot.size;
        if(dist > distLowerBound)return;
        playerAction.heading = getHeadingBetween(world.centerPoint);
    }

    public static int[] getPlayerInfo(GameObject player){
        int info[] = {player.size, player.TorpedoSalvoCount, player.ShieldCount, player.TeleporterCount, player.SupernovaAvailable};
        return info;
    }

    public static void debugBotInfo(PlayerAction playerAction, int debugType){
        /*
        debugType :
        1. tick, execution time, bot info
        2. bot inventory
        3. enemy info
        4. supernova
        */ 
        if(gameState.getWorld().getCurrentTick() == null)return;
        if(debugType == 1){
            System.out.printf("Tick : %d\n", world.getCurrentTick());
            long endTime = System.nanoTime();
            long executionTime = (endTime - startTime) / 1000;
            System.out.print("Execution Time : " + Long.toString(executionTime) + " microseconds\n");
            System.out.printf("Size : %d\n", bot.size);
            System.out.printf("Position : %d %d\n", bot.getPosition().x, bot.getPosition().y);
            System.out.println("Action : " + playerAction.action);
            System.out.printf("Heading : %d\n", playerAction.heading);
            System.out.println();
        }else if(debugType == 2){
            System.out.printf("Tick : %d\n", world.getCurrentTick());
            System.out.printf("Size : %d\n", bot.size);
            System.out.println("Action : " + playerAction.action);
            System.out.printf("Torpedo : %d\n", bot.TorpedoSalvoCount);
            System.out.printf("Teleport : %d\n", bot.TeleporterCount);
            System.out.printf("Shield : %d\n", bot.ShieldCount);
            System.out.printf("Supernova : %d\n", bot.SupernovaAvailable);
            System.out.println();
        }else if(debugType == 3){
            System.out.printf("Tick : %d\n", world.getCurrentTick());
            System.out.printf("Size : %d\n", bot.size);
            System.out.printf("Position : %d %d\n", bot.getPosition().x, bot.getPosition().y);
            System.out.println("Action : " + playerAction.action);
            System.out.println("Enemy info : [Size, Torpedo, Shield, Teleport, Supernova]");
            for(GameObject player : objectList[1]){
                int playerInfo[] = getPlayerInfo(player);
                System.out.println(Arrays.toString(playerInfo));
            }
            System.out.println();
        }else if(debugType == 4){
            if(supernovaFired){
                System.out.printf("Tick : %d\n", world.getCurrentTick());
                for(GameObject gas : objectList[4]){
                    System.out.printf("Size : %d\n", gas.size);
                }
                System.out.println();
                return;
            }
                
            boolean pickedSupernova = false;
            for(GameObject player : objectList[1])if(player.SupernovaAvailable == 1)pickedSupernova = true;
            if(isEmpty(objectList[9]) && !pickedSupernova && isEmpty(objectList[8]))return;
            System.out.printf("Tick : %d\n", world.getCurrentTick());
            if(notEmpty(objectList[8]))
                for(GameObject pickup : objectList[8])
                    System.out.printf("There is pickup in %d,%d size %d\n", pickup.position.x, pickup.position.y, pickup.size);
                    
            if(notEmpty(objectList[9]))
                for(GameObject bomb : objectList[9])
                    System.out.printf("There is bomb in %d,%d size %d\n", bomb.position.x, bomb.position.y, bomb.size);
            if(pickedSupernova)System.out.println("Someone have supernova");
            System.out.println();

        }
    }

}
