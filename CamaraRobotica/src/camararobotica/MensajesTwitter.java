/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package camararobotica;
import java.io.File;
import java.util.List;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.media.ImageUpload;
import twitter4j.media.ImageUploadFactory;
/**
 *
 * @author IDE-MONTERIA
 */
public class MensajesTwitter implements Runnable {
    
    private final String mensaje;
    private final List<String> usuario;
    private final String pathImg;
    private final Twitter twitter;
    private final ImageUpload imgup;
    
    public MensajesTwitter(String msg, List<String> nick, String img){
        
        mensaje = msg;
        usuario = nick;
        pathImg = img;
        
        ConfigurationBuilder cb = new ConfigurationBuilder();
        
        cb.setDebugEnabled(true)
            .setOAuthConsumerKey("bHYEpc5tKI5wccEPt4EkMw")
            .setOAuthConsumerSecret("MEZfyaRuHzPsauPMDw04fVrsTTvZLaMbbWqsKM0FKwA")
            .setOAuthAccessToken("2207684432-DZAlg796XOQg9urQ1RnnXw4cy3IiIbmDbCARPBb")
            .setOAuthAccessTokenSecret("vzHbnMgzRPp3gLmHeT2WJrOkhIFm6rXALVOxGcd9ekUmV");
        
        Configuration config = cb.build();
        
        twitter = new TwitterFactory(config).getInstance();
        
        imgup =  new ImageUploadFactory(config).getInstance();
        
    }

    @Override
    public void run() {
        try {
            
            imgup.upload(new File(pathImg), mensaje);
            
            if(usuario.size() > 0){
                
                for(String user: usuario){
                    twitter.sendDirectMessage(user, mensaje);
                }
            }
                        
        } catch (TwitterException ex) {
            System.err.print("Error:\n"+ex+"\n");
        }
    }
}
