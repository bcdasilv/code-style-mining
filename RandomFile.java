import java.util.*;
import java.io.File;

import java.io.FileInputStream;
import java.awt.swing;

public class classOne {

   public static void main(String[] args) {

      System.out.printf("Hello. I hate the world.\n");

   }
    
    public static int get2() {
        return 2;
    }
    

   public static void crazyIndents() {
		System.out.printf("This is a line with a tab.\n");
      System.out.printf("This line has spaces\n");
    
   }

}

class classTwo {

	private String apple;
	private boolean banana;

   public boolean getName(String name) 
   {
      int abc = 0;

// Multiple if block: Allman style
      if (name.equals("janani")) 
      {
         System.out.printf("hi janani!\n");
      }
      else if (name.equals("kellie"))
      {
         System.out.printf("hi kellie!\n");
      }
      else
      {
         System.out.printf("Who are you?\n");
      }


// Single block: Allman style
      for (int k = 0; k < 4; k++) 
      {

// Multiple block: One-liner style
         if (k % 2 == 0)
            abc++;

// Multiple block: Google style
         if (k % 2 != 0) {
             System.out.printf("good morning!.\n");
         }

// Multiple block: Allman style
         try 
         {
            dummyDeclaration();
         }
         catch (Exception e)
         {
            doSomethingElse();
         }
         finally {
            doSomethingElse();
         }

// Multiple block: Google style
         if (k != 3) {
            System.out.printf("k is not 3\n");
         } else if (k == 2) {
            System.out.printf("k is 2\n");
         }

// Multiple block: Weird style
         try 
         {

            dummyDeclaration();
         } catch (Exception e) 
         {
            doSomethingElse();
         }

// Multiple block: Google style
         try {
            dummyDeclaration();
         } catch (NullPointerException n) {
            doSomethingElse();
         } catch (Exception e) {
            e.printStackTrace();
         } finally {
            doSomethingElse();
         }
         

      }

// Single block with inner multiple block: Google style
      for (int i = 0; i < 5; i++) {
         if (i == 2) {
            System.out.println("True!");
         }         
      }

// Multiple block: One-liner style
      if (true)
         doSomething();
      else if (true)
         doSomethingElse();
      else if (true)
         doAnotherThing();
      else
         whoCaresAnymore();

// Single block: One-liner style
      while (true)
         doNothing();

		return false;
   }


}
