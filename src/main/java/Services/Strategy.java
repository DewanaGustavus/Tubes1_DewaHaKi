package Services;

import Enums.*;
import Models.*;
import Services.BotService;

import java.util.*;
import java.util.stream.*;

public class Strategy {
    public static GameObject bot;
    public static GameState gameState;
    public static Random random = new Random();
    public static final Integer infinity = Integer.MAX_VALUE;
    
    public static void compute(BotService service, PlayerAction playerAction){
        // extract Bot Service
        bot = service.getBot();
        gameState = service.getGameState();

        // extract Game Object
        int objectEnumSize = ObjectTypes.values().length;
        List<GameObject>[] objectList = new List[objectEnumSize+1]; // enum indexing same as Object Types
        List<GameObject> gameObjectList = gameState.getGameObjects();
        List<GameObject> playerList = gameState.getPlayerGameObjects();

        if(!gameObjectList.isEmpty() || !playerList.isEmpty()){
            int gameObjectAmount = gameObjectList.size();
            int playerAmount = playerList.size();

            // create array
            for(int i=1;i<=objectEnumSize;i++){
                objectList[i] = new ArrayList<GameObject>();
            }

            // filter
            for(int i=0;i<gameObjectAmount;i++){
                GameObject gameObject = gameObjectList.get(i);
                int enumIdx = gameObject.getGameObjectType().getValue();
                objectList[enumIdx].add(gameObject);
            }
            
            for(int i=0;i<playerAmount;i++){
                GameObject gameObject = playerList.get(i);
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

        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = random.nextInt(360);
        double distFood = notEmpty(objectList[2]) ? getDistanceBetween(bot, objectList[2].get(0)) : infinity;
        double distCloud = notEmpty(objectList[4]) ? getDistanceBetween(bot, objectList[4].get(0)) : infinity;
        if(distFood != infinity || distCloud != infinity){
            playerAction.heading = distFood < distCloud ? getHeadingBetween(objectList[2].get(0)) : ((getHeadingBetween(objectList[4].get(0)) + 180) % 360);
        }

        int fireChance = random.nextInt(100);
        if(fireChance > 95 && notEmpty(objectList[1], 1)){
            playerAction.action = PlayerActions.FIRETORPEDOES;
            playerAction.heading = getHeadingBetween(objectList[1].get(1));
        }
        service.setPlayerAction(playerAction);
    }

    private static boolean notEmpty(List<GameObject> object){
        return object != null && !object.isEmpty();
    }

    private static boolean notEmpty(List<GameObject> object, int minSize){ // java gak ada default parameter jadi harus overload
        return object != null && object.size() > minSize;
    }
    
    private static double getDistanceBetween(GameObject object1, GameObject object2) {
        int dX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        int dY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(dX * dX + dY * dY);
    }

    private static int getHeadingBetween(GameObject otherObject) {
        int direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private static int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }
}
