package Services;

import Enums.ObjectTypes;
import Enums.PlayerActions;
import Models.GameObject;
import Models.GameState;
import Models.PlayerAction;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Strategy {
    public static GameObject bot;
    public static GameState gameState;
    public static Random random = new Random();
    public static final int infinity = Integer.MAX_VALUE;
    public static HashMap<String, List<GameObject>> objects = new HashMap<>();
    public static HashMap<String, Double> objectsShortestDistance = new HashMap<>();

    public static void compute (BotService service, PlayerAction playerAction) {
        bot = service.getBot();
        gameState = service.getGameState();

        List<GameObject> gameObjectList = gameState.getGameObjects();
        List<GameObject> playerObjectList = gameState.getPlayerGameObjects();

        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = random.nextInt();

        if (!gameObjectList.isEmpty() && !playerObjectList.isEmpty()){
            objects.put("FOOD",getSortedGameObject(bot, gameObjectList, ObjectTypes.FOOD));
            objects.put("SUPERFOOD", getSortedGameObject(bot, gameObjectList, ObjectTypes.SUPERFOOD));
            objects.put("PLAYER", getSortedGameObject(bot, playerObjectList, ObjectTypes.PLAYER));
            objects.put("CLOUD", getSortedGameObject(bot, gameObjectList, ObjectTypes.GASCLOUD));
            objects.put("TORPEDOES", getSortedGameObject(bot, gameObjectList, ObjectTypes.TORPEDOSALVO));
            objects.put("WORMHOLES", getSortedGameObject(bot, playerObjectList, ObjectTypes.WORMHOLE));
            objects.put("TELEPORTER", getSortedGameObject(bot, gameObjectList, ObjectTypes.TELEPORTER));

            calculateShortestDistance(bot);
            movement(bot, playerAction);
            if (notEmpty(objects.get("PLAYER"), 1)) {
                actionTorpedoes(bot, playerAction);
            }
            actionShield(bot, playerAction);
            actionFireTeleporter(bot, playerAction);
            actionTeleport(playerAction);
        }

        service.setPlayerAction(playerAction);
    }

    public static List<GameObject> getSortedGameObject(GameObject bot, List<GameObject> gameObjectList, ObjectTypes objectType){
        return gameObjectList.stream().filter(item -> item.getGameObjectType() == objectType).sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
    }
    public static void calculateShortestDistance(GameObject bot) {
        if (objects != null){
            for (String objectType: objects.keySet()) {
                if (objectType != "PLAYER") {
                    objectsShortestDistance.put(objectType, notEmpty(objects.get(objectType)) ? getDistanceBetween(bot, objects.get(objectType).get(0)) : infinity);
                } else {
                    objectsShortestDistance.put(objectType, notEmpty(objects.get(objectType), 1) ? getDistanceBetween(bot, objects.get(objectType).get(1)) : infinity);
                }
            }
        }
    }


    private static void movement(GameObject bot, PlayerAction playerAction) {
        GameObject worldCenter = new GameObject(gameState.getWorld().centerPoint);
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = getDirection(bot, worldCenter);


        if (objectsShortestDistance.get("PLAYER") != infinity && getDistanceBetween(bot, objects.get("PLAYER").get(1)) < 1.5 * bot.size) {
            playerAction.heading = getOppositeDirection(bot, objects.get("PLAYER").get(1));
            System.out.println("Other player is too large, run!!");
        } else if(getDistanceBetween(bot, worldCenter) + (1.5 * bot.size) > gameState.world.radius) {
            playerAction.heading = getDirection(bot, worldCenter);
            System.out.println("Near edge, moving to center.");
        } else if (objectsShortestDistance.get("CLOUD") != infinity && objectsShortestDistance.get("CLOUD") < objectsShortestDistance.get("FOOD") && objectsShortestDistance.get("CLOUD") < objectsShortestDistance.get("SUPERFOOD")) {
            playerAction.heading = getOppositeDirection(bot, objects.get("CLOUD").get(0));
            System.out.println("Go away from gas cloud");
        } else if (objectsShortestDistance.get("SUPERFOOD") != infinity && objectsShortestDistance.get("SUPERFOOD") < objectsShortestDistance.get("FOOD")) {
            playerAction.heading = getDirection(bot, objects.get("SUPERFOOD").get(0));
            System.out.println("Moving to nearest super food");
        } else if (objectsShortestDistance.get("FOOD") != infinity) {
            playerAction.heading = getDirection(bot, objects.get("FOOD").get(0));
            System.out.println("Moving to nearest food");
        } else {
            System.out.println("Moving to center, no target found.");
        }
    }

    private static void actionTorpedoes(GameObject bot, PlayerAction playerAction) {
        if (bot.size > 10 && getDistanceBetween(bot, objects.get("PLAYER").get(1)) < 650 && bot.torpedoSalvoCount > 4) {
            playerAction.action = PlayerActions.FIRETORPEDOES;
            playerAction.heading = getDirection(bot, objects.get("PLAYER").get(1)) + 5;
            System.out.println("Firing torpedoes!!");
        } else if (bot.size > 10 && objectsShortestDistance.get("TORPEDOES") != infinity && objectsShortestDistance.get("TORPEDOES") < 1.2 * bot.size && bot.torpedoSalvoCount > 3){
            playerAction.action = PlayerActions.FIRETORPEDOES;
            playerAction.heading = getDirection(bot, objects.get("TORPEDOES").get(0));
            System.out.println("Firing torpedoes for defense :)))");
        }
    }

    private static void actionShield(GameObject bot, PlayerAction playerAction) {
        if (objectsShortestDistance.get("TORPEDOES") != infinity && bot.shieldCount > 0 && objectsShortestDistance.get("TORPEDOES") < 1.2 * bot.size) {
            playerAction.action = PlayerActions.ACTIVATESHIELD;
            System.out.println("Activating shield!");
        }
    }

    private static void actionFireTeleporter(GameObject bot, PlayerAction playerAction) {
        if (bot.size > 120 && objectsShortestDistance.get("PLAYER") != infinity) {
            for (GameObject player : objects.get("PLAYER")) {
                if (player.size < bot.size/2 && bot.teleporterCount > 0) {
                    playerAction.heading = getDirection(bot, player) + 5;
                    playerAction.action = PlayerActions.FIRETELEPORT;
                    System.out.println("Firing teleporter!!");
                    return;
                }
            }
        }
    }


    private static void actionTeleport(PlayerAction playerAction) {
        if (!objects.get("TELEPORTER").isEmpty() && objectsShortestDistance.get("PLAYER") != infinity) {
            for (GameObject teleporter : objects.get("TELEPORTER")) {
                for (GameObject player : objects.get("PLAYER")) {
                    if (getDistanceBetween(player, teleporter) - (0.5 * bot.size) < 30) {
                        playerAction.action = PlayerActions.TELEPORT;
                        System.out.println("PUFT, teleporting!!");
                        return;
                    }
                }
            }
        }
    }


    private static boolean notEmpty(List<GameObject> object){
        return object != null && !object.isEmpty();
    }

    private static boolean notEmpty(List<GameObject> object, int minSize){
        return object != null && object.size() > minSize;
    }

    private static double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    private static int getDirection(GameObject bot, GameObject gameObject) {
        int cartesianDegrees = toDegrees(Math.atan2(gameObject.position.y - bot.position.y, gameObject.position.x - bot.position.x));
        return (cartesianDegrees + 360) % 360;
    }

    private static int getOppositeDirection(GameObject gameObject1, GameObject gameObject2) {
        return (toDegrees(Math.atan2(gameObject2.position.y - gameObject1.position.y, gameObject2.position.x - gameObject1.position.x)) + 180) % 360;
    }

    private static int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }
}
