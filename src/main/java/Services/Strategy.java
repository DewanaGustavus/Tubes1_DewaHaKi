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

    public static void compute(BotService service, PlayerAction playerAction){
        extractBotService(service);
        extractGameObject();

        defaultAction(playerAction);
        moveLogic(playerAction);
        fireTorpedoLogic(playerAction);
        fireTeleporterLogic(playerAction);
        teleportLogic(playerAction);
        shieldLogic(playerAction);

        debugBotInfo();

        service.setPlayerAction(playerAction);
    }

    private static boolean notEmpty(List<GameObject> object){
        return object != null && !object.isEmpty();
    }

    private static boolean notEmpty(List<GameObject> object, int minSize){ // java gak ada default parameter jadi harus overload
        return object != null && object.size() > minSize;
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

        if(notEmpty(gameObjectList) || notEmpty(playerList)){
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
                objectList[i] = objectList[i]
                                        .stream()
                                        .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item)))
                                        .collect(Collectors.toList());
            }
        }

        // get strong and weak enemy
        weakEnemy = new ArrayList<GameObject>();
        strongEnemy = new ArrayList<GameObject>();
		int weakEnemySizeOffset = 20;
        if(notEmpty(objectList[1], 1)){
            for(int i=1;i<objectList[1].size();i++){
                if(bot.size >= objectList[1].get(i).size + weakEnemySizeOffset){
                    weakEnemy.add(objectList[1].get(i));
                }else{
                    strongEnemy.add(objectList[1].get(i));
                }
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
        towardObject.add(objectList[7]); // food
        towardObject.add(objectList[8]); // supernova pick
        towardObject.add(weakEnemy);

        List<List<GameObject>> avoidObject = new ArrayList<List<GameObject>>();
        avoidObject.add(objectList[4]); // gas
        avoidObject.add(objectList[5]); // asteroid
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
        double distLowerBound = 700;
        if(bot.size < minimumSize)return;
        int fireChance = random.nextInt(100);
        if(bot.TorpedoSalvoCount >= 4)fireChance = random.nextInt(80, 100); // maximum is 5, dont waste
        if(fireChance > 95 && notEmpty(objectList[1], 1)){
            for(int i=1;i<objectList[1].size();i++){
				double dist = getDistanceBetween(bot, objectList[1].get(i)) - objectList[i].get(i).size;
                if(dist >= distLowerBound){
                    break;
                }
                playerAction.action = PlayerActions.FIRETORPEDOES;
                playerAction.heading = getHeadingBetween(objectList[1].get(i));
                break;
            }
        }
    }

    public static void fireTeleporterLogic(PlayerAction playerAction){
        double minimumSize = 100;
		int fireChance = random.nextInt(100);
        if(!notEmpty(weakEnemy) || bot.TeleporterCount == 0 || bot.size < minimumSize || notEmpty(objectList[10]) || fireChance < 90)return;
        double distLowerBound = 1000;
        double weakSizeMultiplier = 0.8;
        GameObject target = null;
        for(GameObject enemy : weakEnemy){
            if(enemy.size > (bot.size - 20) * weakSizeMultiplier){
                continue;
            }
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
        if(notEmpty(objectList[10])){
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
    }

    public static void shieldLogic(PlayerAction playerAction){
        int minimumSize = 100;
        if(bot.ShieldCount == 0 || bot.size < minimumSize)return;
        double distLowerBound = 50;
        double toleratedAngle = 30;
        boolean useShield = false;
        for(GameObject torpedo : objectList[6]){
            double dist = getDistanceBetween(bot, torpedo) - bot.size;
            if(dist > distLowerBound)continue;
            int angle1 = Math.abs(torpedo.currentHeading - ((getHeadingBetween(torpedo) + 180) % 360));
            int angle2 = Math.abs(torpedo.currentHeading - getHeadingBetween(torpedo));
            if(angle1 < toleratedAngle || angle2 < toleratedAngle){
                useShield = true;
                break;
            }
        }
        if(useShield)playerAction.action = PlayerActions.ACTIVATESHIELD;
    }

    public static void runFromBorder(PlayerAction playerAction){
        double distLowerBound = 30;
        if(world == null || world.getRadius() == null)return;
        double dist = world.radius - getDistanceBetween(bot.getPosition(), world.centerPoint) - bot.size;
        if(dist > distLowerBound)return;
        playerAction.heading = getHeadingBetween(world.centerPoint);
    }

    public static void debugBotInfo(){
    }

}
