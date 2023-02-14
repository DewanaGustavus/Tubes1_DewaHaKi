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
    

    public static void compute(BotService service, PlayerAction playerAction){
        startTime = System.nanoTime();
        extractBotService(service);
        extractGameObject();

        defaultAction(playerAction);
        moveLogic(playerAction);
        fireTorpedoLogic(playerAction);
        fireTeleporterLogic(playerAction);
        teleportLogic(playerAction);
        shieldLogic(playerAction);
        

        // debugBotInfo(playerAction);
        service.setPlayerAction(playerAction);
    }

    private static boolean notEmpty(List<GameObject> object){
        return object != null && !object.isEmpty();
    }

    private static boolean notEmpty(List<GameObject> object, int minSize){ // java gak ada default parameter jadi harus overload
        return object != null && object.size() > minSize;
    }

    private static boolean isEmpty(List<GameObject> object){
        return !notEmpty(object);
    }

    private static boolean isEmpty(List<GameObject> object, int minSize){
        return !notEmpty(object, 1);
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
        if(isEmpty(objectList[1], 1))return;
        for(int i=1;i<objectList[1].size();i++){
            if(bot.size >= objectList[1].get(i).size + weakEnemySizeOffset){
                weakEnemy.add(objectList[1].get(i));
            }else{
                strongEnemy.add(objectList[1].get(i));
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
        /*** 
        int angle1=30;
        if(!isEmpty(objectList[6]))angle1 = Math.abs(objectList[6].get(0).currentHeading - ((getHeadingBetween(objectList[6].get(0)) + 180) % 360));
        if (bot.size > 20 && getDistanceBetween(bot, objectList[1].get(0)) < 450 && bot.TorpedoSalvoCount > 4) {
            playerAction.action = PlayerActions.FIRETORPEDOES;
            playerAction.heading = getHeadingBetween(objectList[1].get(0));
            System.out.println("Firing torpedoes!!");
        } 
        else if (bot.size > 20 && !isEmpty(objectList[6]) && angle1<=20 && getDistanceBetween(bot, objectList[6].get(0)) < 1.2 * bot.size && bot.TorpedoSalvoCount > 3){
            playerAction.action = PlayerActions.FIRETORPEDOES;
            playerAction.heading = getHeadingBetween(objectList[6].get(0));
            System.out.println("Firing torpedoes for defense :)))");
        }

        int minimumSize = 50;
        double distLowerBound = 200;
        int fireChance = random.nextInt(100);
        if(bot.size>minimumSize && bot.TorpedoSalvoCount>2 && !isEmpty(objectList[1])){
        for(int i=1;i<objectList[1].size();i++){
            double dist = getDistanceBetween(bot, objectList[1].get(i)) - objectList[1].get(i).size-bot.size;
            if(dist < distLowerBound){
            playerAction.action = PlayerActions.FIRETORPEDOES;
            playerAction.heading = getHeadingBetween(objectList[1].get(i));
            break;
            }
            
        }
        }
        ***/
        int minimumSize = 50;
        double distLowerBound = 600;
        int fireChance = random.nextInt(100);
        if(bot.TorpedoSalvoCount >= 3)fireChance = random.nextInt(40, 100); // maximum is 5, dont waste
        if(bot.TorpedoSalvoCount >= 2 && getDistanceBetween(bot, objectList[1].get(1))<100)fireChance = random.nextInt(75, 100);
        if(bot.size < minimumSize || fireChance < 97 || isEmpty(objectList[1], 1))return;
        for(int i=1;i<objectList[1].size();i++){
            double dist = getDistanceBetween(bot, objectList[1].get(i)) - objectList[1].get(i).size;
            if(dist >= distLowerBound){
                break;
            }
            playerAction.action = PlayerActions.FIRETORPEDOES;
            playerAction.heading = getHeadingBetween(objectList[1].get(i));
            break;
        }
    }

    public static void fireTeleporterLogic(PlayerAction playerAction){
        double minimumSize = 80;
		int fireChance = random.nextInt(100);
        if (bot.size>250) fireChance=random.nextInt(40,100);
        if(isEmpty(weakEnemy) || bot.TeleporterCount == 0 || bot.size < minimumSize 
            || notEmpty(objectList[10]) || fireChance < 97)return;
        double distLowerBound = 1200;
        double weakSizeMultiplier = 0.9;
        GameObject target = null;
        for(GameObject enemy : weakEnemy){
            // System.out.println((bot.size - 20) * weakSizeMultiplier);
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
        double distLowerBound = 20; 
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
        if(bot.ShieldCount == 0 || bot.size < minimumSize)return;
        double distLowerBound = 100;
        double toleratedAngle = 30;
        boolean useShield = false;
        if(notEmpty(objectList[6])){
        for(GameObject torpedo : objectList[6]){
            double dist = getDistanceBetween(bot, torpedo) - bot.size - torpedo.size;
            int angle1 = Math.abs(torpedo.currentHeading - ((getHeadingBetween(torpedo) + 180) % 360));
            // System.out.printf("dist : %d\n", (int)dist);
            // System.out.printf("dist : %d, angle : %d\n", (int)dist, angle1);
            if(dist > distLowerBound || (angle1 > toleratedAngle && angle1 < (360-toleratedAngle)) )continue;
            // int angle2 = Math.abs(torpedo.currentHeading - getHeadingBetween(torpedo));
            // System.out.printf("angel : %d %d\n", angle1, angle2);
            useShield = true;
            break;
        }
        }
        if(useShield)playerAction.action = PlayerActions.ACTIVATESHIELD;
    }

    public static void runFromBorder(PlayerAction playerAction){
        double distLowerBound = 20 ;
        if(world.getCurrentTick()!=null)distLowerBound+=world.getCurrentTick()/5;
        if(world == null || world.getRadius() == null)return;
        double dist = world.radius - getDistanceBetween(bot.getPosition(), world.centerPoint) - bot.size;
        // System.out.printf("%d %d %d %d\n", world.radius, bot.getPosition().x, bot.getPosition().y, bot.size);
        if(dist > distLowerBound)return;
        playerAction.heading = getHeadingBetween(world.centerPoint);
        System.out.printf("gocenter");
    }

    public static void debugBotInfo(PlayerAction playerAction){
        if(notEmpty(objectList[1])){
            System.out.printf("Tick : %d\n", world.getCurrentTick());
            long endTime = System.nanoTime();
            long executionTime = (endTime - startTime) / 1000;
            System.out.print("Execution Time : " + Long.toString(executionTime) + " microseconds\n");
            System.out.printf("Size : %d\n", bot.size);
            System.out.printf("Position : %d %d\n", bot.getPosition().x, bot.getPosition().y);
            System.out.println("Action : " + playerAction.action);
            System.out.printf("Heading : %d\n\n", playerAction.heading);
        }
    }

}
