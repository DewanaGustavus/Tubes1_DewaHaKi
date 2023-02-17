package Enums;

public class Effects{
    public int value;
    public final int afterburner = 0;
    public final int asteroid = 1;
    public final int gas = 2;
    public final int superfood = 3;
    public final int shield = 4;

    public Effects(int num){
        value = num;
    }
    public boolean haveMask(int idx){
        int mask = value & (1<<idx);
        return mask >= 1;
    }
    public boolean haveAfterburner(){
        return haveMask(afterburner);        
    }
    public boolean haveAsteroid(){
        return haveMask(asteroid);        
    }
    public boolean haveGas(){
        return haveMask(gas);        
    }
    public boolean haveSuperfood(){
        return haveMask(superfood);        
    }
    public boolean haveShield(){
        return haveMask(shield);        
    }
}