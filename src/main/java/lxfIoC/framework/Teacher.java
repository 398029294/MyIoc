package lxfIoC.framework;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lxfIoC.framework.annotion.Autowired;
import lxfIoC.framework.annotion.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class Teacher {

    @Autowired
   private IDrink water;

    @Autowired
   private Coffee coffee;

//    public void  drink(){
//        System.out.println(water.getType());
//    }

}
