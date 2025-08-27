package cz.psgs.SpringRestDemo.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cz.psgs.SpringRestDemo.model.Photo;
import cz.psgs.SpringRestDemo.repository.PhotoRepository;
import cz.psgs.SpringRestDemo.util.appUtils.AppUtil;

@Service
public class PhotoService {

    private static final String PHOTOS_FOLDER = "photos";
    private static final String THUMBNAILS_FOLDER = "thumbnails";

    @Autowired
    private PhotoRepository photoRepository;

    public Photo save (Photo photo){
        return photoRepository.save(photo);
    }

    public void delete(Photo photo){
        photoRepository.delete(photo);
    }

    public Optional<Photo> findById(long id){
        return photoRepository.findById(id);
    }

    public List<Photo> findByAlbumId(long id){
        return photoRepository.findByAlbum_id(id);
    }

    public void deletePhotoAndFiles(Photo photo) {
        long albumId = photo.getAlbum().getId();
        AppUtil.deletePhotoFromPath(photo.getFileName(), PHOTOS_FOLDER, albumId);
        AppUtil.deletePhotoFromPath(photo.getFileName(), THUMBNAILS_FOLDER, albumId);
        photoRepository.delete(photo);
    }

    public void deletePhotoOnlyFromDb(Photo photo) {
        photoRepository.delete(photo);
    }

    public List<Photo> findAll() {
        return photoRepository.findAll();
    }

}
