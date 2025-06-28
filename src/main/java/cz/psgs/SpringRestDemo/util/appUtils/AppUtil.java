package cz.psgs.SpringRestDemo.util.appUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import org.imgscalr.Scalr;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

public class AppUtil {

    public static String PATH = "src\\main\\resources\\static\\uploads\\";

    public static String getPhotoUploadPath (String fileName, String folder_name, long album_id) throws IOException{
        String path = PATH + album_id + "\\" + folder_name;
        
        Files.createDirectories(Paths.get(path));
        
        return new File(path).getAbsolutePath() + "\\" + fileName;
    
    }

    public static BufferedImage getThumbnail (MultipartFile originalFile, Integer width) throws IOException{
        BufferedImage thumbImg = null;
        BufferedImage img = ImageIO.read(originalFile.getInputStream());
        thumbImg = Scalr.resize(img, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, width, Scalr.OP_ANTIALIAS);
        return thumbImg;
    }

    public static Resource getFileAsResource(long album_id, String folderName, String fileName) throws IOException{
        String location = "src\\main\\resources\\static\\uploads\\" + album_id + "\\" + folderName;
        File file = new File(location);
        if (file.exists()){
            Path path = Paths.get(file.getAbsolutePath());
            return new UrlResource(path.toUri());
        } else {
            return null;
        }
    }

    public static boolean deletePhotoFromPath(String fileName, String folderName, long album_id) {
        try {
            File f = new File(PATH + album_id + "\\" + folderName + "\\" + fileName); // file to delete
            if (f.delete()){
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
