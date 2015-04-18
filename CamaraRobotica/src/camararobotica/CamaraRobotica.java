/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package camararobotica;


import javax.swing.JFrame;
import org.jvnet.substance.SubstanceLookAndFeel;
import camararobotica.GUICamaraRobot;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;

/**
 *
 * @author IDE-MONTERIA
 */
public class CamaraRobotica {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
        
        public void run(){
            JFrame.setDefaultLookAndFeelDecorated(true);
            SubstanceLookAndFeel.setSkin("org.jvnet.substance.skin.TwilightSkin");
            try {
                new GUICamaraRobot().setVisible(true);
            } catch (Exception ex) {
                Logger.getLogger(CamaraRobotica.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        };

           
        });
       
        
    }
    
   
}
