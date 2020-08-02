package lxfIoC.framework;


import lxfIoC.framework.annotion.Component;

@Component
public class Water implements IDrink{

    public  String getType(){

        return "白开水";

    }



}
