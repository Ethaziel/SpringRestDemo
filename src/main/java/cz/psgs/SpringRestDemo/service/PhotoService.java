package cz.psgs.SpringRestDemo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cz.psgs.SpringRestDemo.model.Photo;
import cz.psgs.SpringRestDemo.repository.PhotoRepository;

@Service
public class PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    public Photo save (Photo photo){
        return photoRepository.save(photo);
    }

}
