package cz.psgs.SpringRestDemo.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import cz.psgs.SpringRestDemo.model.Photo;
import cz.psgs.SpringRestDemo.service.PhotoService;
import cz.psgs.SpringRestDemo.util.appUtils.AppUtil;
import jakarta.annotation.PostConstruct;

@Profile("prod")
@Component
public class DBCleaner {

    private static final Logger log = LoggerFactory.getLogger(DBCleaner.class);

    private final PhotoService photoService;

    @Autowired
    public DBCleaner(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostConstruct
    public void cleanupOrphanedPhotos() {
        List<Photo> allPhotos = photoService.findAll();
        int removed = 0;

        log.info("DBCleaner: Starting cleanup, found {} photos in DB", allPhotos.size());

        for (Photo photo : allPhotos) {
            try {
                Resource resource = AppUtil.getFileAsResource(
                        photo.getAlbum().getId(),
                        "photos",
                        photo.getFileName()
                );

                if (resource == null || !resource.exists()) {
                    // File missing → remove DB row only
                    photoService.deletePhotoOnlyFromDb(photo);
                    removed++;
                    log.warn("DBCleaner: Removed orphaned photo entry id={} (file missing: {})",
                             photo.getId(), photo.getFileName());
                }

            } catch (Exception e) {
                log.error("DBCleaner: Error checking photo id={}, removing from DB", photo.getId(), e);
                photoService.deletePhotoOnlyFromDb(photo);
                removed++;
            }
        }

        log.info("DBCleaner: Finished cleanup, removed {} orphaned photo entries", removed);
    }
}

