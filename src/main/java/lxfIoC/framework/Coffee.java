package lxfIoC.framework;


import lxfIoC.framework.annotion.Component;

@Component
public class Coffee implements IDrink{

  public  String getType(){
      return "黑咖啡";
  }

}
