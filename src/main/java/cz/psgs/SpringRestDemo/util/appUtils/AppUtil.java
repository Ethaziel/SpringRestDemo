package cz.psgs.SpringRestDemo.util.appUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AppUtil {
    public static String getPhotoUploadPath (String fileName, long album_id) throws IOException{
        Files.createDirectories(Paths.get("src\\main\\resources\\static\\uploads\\" + album_id));
        
        return new File("src\\main\\resources\\static\\uploads\\" + album_id).getAbsolutePath() + "\\" + fileName;
    
    }
}
