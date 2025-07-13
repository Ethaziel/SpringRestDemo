package cz.psgs.SpringRestDemo.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cz.psgs.SpringRestDemo.model.Account;
import cz.psgs.SpringRestDemo.model.Album;
import cz.psgs.SpringRestDemo.model.Photo;
import cz.psgs.SpringRestDemo.payload.auth.album.AlbumPayloadDTO;
import cz.psgs.SpringRestDemo.payload.auth.album.AlbumViewDTO;
import cz.psgs.SpringRestDemo.payload.auth.album.PhotoDTO;
import cz.psgs.SpringRestDemo.payload.auth.album.PhotoPayloadDTO;
import cz.psgs.SpringRestDemo.payload.auth.album.PhotoViewDTO;
import cz.psgs.SpringRestDemo.service.AccountService;
import cz.psgs.SpringRestDemo.service.AlbumService;
import cz.psgs.SpringRestDemo.service.PhotoService;
import cz.psgs.SpringRestDemo.util.appUtils.AppUtil;
import cz.psgs.SpringRestDemo.util.constants.AlbumError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true", maxAge = 3600)
@RequestMapping("/")
@Tag(name = "Album Controller", description = "Controller for album and photo management")
@Slf4j
public class AlbumController {

    static final String PHOTOS_FOLDER_NAME = "photos";
    static final String THUMBNAIL_FOLDER_NAME = "thumbnails";
    static final int THUMBNAIL_WIDTH = 300;


    @Autowired
    private AccountService accountService;

    @Autowired
    private AlbumService albumService;

    @Autowired
    private PhotoService photoService;

    @PostMapping(value = "albums/add", produces = "application/json", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "Please add valid name and description")
    @ApiResponse(responseCode = "201", description = "Album added")
    @Operation(summary = "Add an album")
    @SecurityRequirement(name = "psgs-demo-api")
    public ResponseEntity<AlbumViewDTO> addAlbum(@Valid @RequestBody AlbumPayloadDTO albumPayloadDTO, Authentication authentication){
        try {
            Album album = new Album();
            album.setName(albumPayloadDTO.getName());
            album.setDescription(albumPayloadDTO.getDescription());
            String email = authentication.getName();
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();
            album.setAccount(account);
            album = albumService.save(album);
            AlbumViewDTO albumViewDTO = new AlbumViewDTO(album.getId(), album.getName(), album.getDescription(), null);
            return ResponseEntity.ok(albumViewDTO);

        } catch (Exception e) {
            log.debug(AlbumError.ADD_ALBUM_ERROR.toString() + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    @PutMapping(value = "albums/{album_id}/update", produces = "application/json", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "Please add valid name and description")
    @ApiResponse(responseCode = "204", description = "Album updated")
    @Operation(summary = "Update an album")
    @SecurityRequirement(name = "psgs-demo-api")
    public ResponseEntity<AlbumViewDTO> updateAlbum(@Valid @RequestBody AlbumPayloadDTO albumPayloadDTO, 
                        @PathVariable long album_id, Authentication authentication){
        try {
            String email = authentication.getName();
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();

            Optional<Album> optionalAlbum = albumService.findById(album_id);
            Album album;
            if (optionalAlbum.isPresent()){
                album = optionalAlbum.get();
                if (account.getId() != album.getAccount().getId()){
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            
            album.setName(albumPayloadDTO.getName());
            album.setDescription(albumPayloadDTO.getDescription());
            album = albumService.save(album);

            List<PhotoDTO> photos = new ArrayList<>();
            for (Photo photo : photoService.findByAlbumId(album.getId())){
                String link = "/albums/" + album.getId() + "/photos/" + photo.getId() + "/download-photo";
                photos.add(new PhotoDTO(photo.getId(), photo.getName(), photo.getDescription(), 
                                        photo.getFileName(), link));
                
            }

            AlbumViewDTO albumViewDTO = new AlbumViewDTO(album.getId(), album.getName(), album.getDescription(), photos);
            return ResponseEntity.ok(albumViewDTO);

        } catch (Exception e) {
            log.debug(AlbumError.ADD_ALBUM_ERROR.toString() + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping(value = "albums/", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "List of albums")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token error")
    @Operation(summary = "List of albums API")
    @SecurityRequirement(name = "psgs-demo-api")
    public List<AlbumViewDTO> albums(Authentication authentication){
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account account = optionalAccount.get();

        List<AlbumViewDTO> albums = new ArrayList<>();
        for(Album album : albumService.findByAccount_id(account.getId())){
            

            List<PhotoDTO> photos = new ArrayList<>();
            for (Photo photo : photoService.findByAlbumId(album.getId())){
                String link = "/albums/" + album.getId() + "/photos/" + photo.getId() + "/download-photo";
                photos.add(new PhotoDTO(photo.getId(), photo.getName(), photo.getDescription(), 
                                        photo.getFileName(), link));
                
            }
            albums.add(new AlbumViewDTO(album.getId(), album.getName(), album.getDescription(), photos));
            
        }

        return albums;
    }

    @GetMapping(value = "albums/{album_id}", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "List of albums")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token error")
    @Operation(summary = "Show album by album id")
    @SecurityRequirement(name = "psgs-demo-api")
    public ResponseEntity<AlbumViewDTO> albumsById(@PathVariable long album_id, Authentication authentication){
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account account = optionalAccount.get();

        Optional<Album> optionalAlbum = albumService.findById(album_id);
        Album album;

        if(optionalAlbum.isPresent()){
            album = optionalAlbum.get();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        if (account.getId() != album.getAccount().getId()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<PhotoDTO> photos = new ArrayList<>();
        for (Photo photo : photoService.findByAlbumId(album.getId())){
            String link = "/albums/" + album.getId() + "/photos/" + photo.getId() + "/download-photo";
            photos.add(new PhotoDTO(photo.getId(), photo.getName(), photo.getDescription(), 
                                    photo.getFileName(), link));
            
        }
        AlbumViewDTO albumViewDTO = new AlbumViewDTO(album.getId(), album.getName(), album.getDescription(), photos);
        return ResponseEntity.ok(albumViewDTO);
    }

    @PostMapping(value = "albums/{album_id}/upload-photos", consumes = {"multipart/form-data"})
    @ApiResponse(responseCode = "400", description = "Please check the payload or token")
    @Operation(summary = "Upload photos into album")
    @SecurityRequirement(name = "psgs-demo-api")
    public ResponseEntity<List<HashMap<String, List<?>>>> photos(@RequestPart(required = true) MultipartFile[] files, @PathVariable long album_id, Authentication authentication){
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account account = optionalAccount.get();
        Optional<Album> optionalAlbum = albumService.findById(album_id);
        Album album;
        if (optionalAlbum.isPresent()){
            album = optionalAlbum.get();
            if (account.getId() != album.getAccount().getId()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        
        
        List<PhotoViewDTO> fileNamesWithSuccess = new ArrayList<>();
        List<String> fileNamesWithError = new ArrayList<>();


        Arrays.asList(files).stream().forEach(file -> {
            String contentType = file.getContentType();
            if (contentType.equals("image/png") || contentType.equals("image/jpg") || contentType.equals("image/jpeg")){
                //fileNamesWithSuccess.add(file.getOriginalFilename());
                int length = 10;
                /* boolean useLetters = true;
                boolean useNumbers = true; */
                try {
                    String fileName = file.getOriginalFilename();
                    RandomStringGenerator generator = new RandomStringGenerator.Builder()
                    .withinRange('a', 'z')
                    .build();
                    String generatedString = generator.generate(length);
                    String finalPhotoName = generatedString + fileName;
                    String absoluteFileLocation = AppUtil.getPhotoUploadPath(finalPhotoName, PHOTOS_FOLDER_NAME, album_id);
                    Path path = Paths.get(absoluteFileLocation);
                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                    Photo photo = new Photo();
                    photo.setName(fileName);
                    photo.setFileName(finalPhotoName);
                    photo.setOriginalFileName(fileName);
                    photo.setAlbum(album);
                    photoService.save(photo);

                    PhotoViewDTO photoViewDTO = new PhotoViewDTO(photo.getId(), photo.getName(), photo.getDescription());
                    fileNamesWithSuccess.add(photoViewDTO);

                    BufferedImage thumbImage = AppUtil.getThumbnail(file, THUMBNAIL_WIDTH);

                    File thumbnailLocation = new File(AppUtil.getPhotoUploadPath(finalPhotoName, THUMBNAIL_FOLDER_NAME, album_id));
                    ImageIO.write(thumbImage, file.getContentType().split("/")[1], thumbnailLocation);

                } catch (Exception e) {
                    log.debug(AlbumError.PHOTO_UPLOAD_ERROR.toString() + ": " + e.getMessage());
                    fileNamesWithError.add(file.getOriginalFilename());
                }
            } else {
                fileNamesWithError.add(file.getOriginalFilename());
            }
            
        
        });

        HashMap<String, List<?>> result = new HashMap<>();
        result.put("SUCCESS", fileNamesWithSuccess);
        result.put("ERRORS", fileNamesWithError);

        List<HashMap<String, List<?>>> response = new ArrayList<>();
        response.add(result);

        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "albums/{album_id}/photos/{photo_id}/update", produces = "application/json", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "Please add valid name and description")
    @ApiResponse(responseCode = "204", description = "Photo updated")
    @Operation(summary = "Update a photo")
    @SecurityRequirement(name = "psgs-demo-api")
    public ResponseEntity<PhotoViewDTO> updatePhoto(@Valid @RequestBody PhotoPayloadDTO photoPayloadDTO, 
                        @PathVariable long album_id, @PathVariable long photo_id, Authentication authentication){
        try {
            String email = authentication.getName();
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();

            Optional<Album> optionalAlbum = albumService.findById(album_id);
            Album album;
            if (optionalAlbum.isPresent()){
                album = optionalAlbum.get();
                if (account.getId() != album.getAccount().getId()){
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            
            Optional<Photo> optionalPhoto = photoService.findById(photo_id);
            if (optionalPhoto.isPresent()){
                Photo photo = optionalPhoto.get();
                if (photo.getAlbum().getId() != album_id){
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
                photo.setName(photoPayloadDTO.getName());
                photo.setDescription(photoPayloadDTO.getDescription());
                photoService.save(photo);
                PhotoViewDTO photoViewDTO = new PhotoViewDTO(photo.getId(), photo.getName(), photo.getDescription());
                return ResponseEntity.ok(photoViewDTO);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

        } catch (Exception e) {
            log.debug(AlbumError.ADD_ALBUM_ERROR.toString() + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("albums/{album_id}/photos/{photo_id}/download-photo")
    @SecurityRequirement(name = "psgs-demo-api")
    @Operation(summary = "Download photo")
    @ApiResponse(responseCode = "200", description = "Photo file", content = @Content(mediaType = "application/octet-stream"))
    public ResponseEntity<?> downloadPhoto(@PathVariable("album_id") long album_id, 
                        @PathVariable("photo_id") long photo_id, Authentication authentication){
        
        return downloadFile(album_id, photo_id, PHOTOS_FOLDER_NAME, authentication);
    }

    @GetMapping("albums/{album_id}/photos/{photo_id}/download-thumbnail")
    @SecurityRequirement(name = "psgs-demo-api")
    @Operation(summary = "Download thumbnail")
    @ApiResponse(responseCode = "200", description = "Thumbnail file", content = @Content(mediaType = "application/octet-stream"))
    public ResponseEntity<?> downloadThumbnail(@PathVariable("album_id") long album_id, 
                        @PathVariable("photo_id") long photo_id, Authentication authentication){
        
        return downloadFile(album_id, photo_id, THUMBNAIL_FOLDER_NAME, authentication);

    }

    public ResponseEntity<?> downloadFile(long album_id, long photo_id, String folderName, Authentication authentication){
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account account = optionalAccount.get();
        Optional<Album> optionalAlbum = albumService.findById(album_id);
        Album album;
        if (optionalAlbum.isPresent()){
            album = optionalAlbum.get();
            if (account.getId() != album.getAccount().getId()){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        Optional<Photo> optionalPhoto = photoService.findById(photo_id);
        if(optionalPhoto.isPresent()){
            Photo photo = optionalPhoto.get();
            if (photo.getAlbum().getId() != album_id){
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            Resource resource = null;
            try {
                resource = AppUtil.getFileAsResource(album_id, folderName, photo.getFileName());

            } catch (Exception e) {
                return ResponseEntity.internalServerError().build();
            }
            if (resource == null){
                return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
            }

            String contentType;
            try {
                contentType = Files.probeContentType(resource.getFile().toPath());
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
            } catch (IOException e) {
                // Log the error and fallback to default
                contentType = "application/octet-stream";
            }
            String headerValue = "inline; filename=\"" + photo.getOriginalFileName() + "\"";

            return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                        .body(resource);

        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping(value = "albums/{album_id}/photos/{photo_id}/delete")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "202", description = "Photo deleted")
    @Operation(summary = "Delete a photo")
    @SecurityRequirement(name = "psgs-demo-api")
    public ResponseEntity<String> deletePhoto(@PathVariable long album_id, 
                    @PathVariable long photo_id, Authentication authentication){
        try {
            String email = authentication.getName();
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();
            Optional<Album> optionalAlbum = albumService.findById(album_id);
            Album album;
            if (optionalAlbum.isPresent()){
                album = optionalAlbum.get();
                if (account.getId() != album.getAccount().getId()){
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            Optional<Photo> optionalPhoto = photoService.findById(photo_id);
            if(optionalPhoto.isPresent()){
                Photo photo = optionalPhoto.get();
                if (photo.getAlbum().getId() != album_id){
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }

                AppUtil.deletePhotoFromPath(photo.getFileName(), PHOTOS_FOLDER_NAME, album_id);
                AppUtil.deletePhotoFromPath(photo.getFileName(), THUMBNAIL_FOLDER_NAME, album_id);
                photoService.delete(photo);

                return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping(value = "albums/{album_id}/delete")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "202", description = "Album deleted")
    @Operation(summary = "Delete an album")
    @SecurityRequirement(name = "psgs-demo-api")
    public ResponseEntity<String> deleteAlbum(@PathVariable long album_id, Authentication authentication){
        try {
            String email = authentication.getName();
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();
            Optional<Album> optionalAlbum = albumService.findById(album_id);
            Album album;
            if (optionalAlbum.isPresent()){
                album = optionalAlbum.get();
                if (account.getId() != album.getAccount().getId()){
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            for (Photo photo : photoService.findByAlbumId(album.getId())){
                AppUtil.deletePhotoFromPath(photo.getFileName(), PHOTOS_FOLDER_NAME, album_id);
                AppUtil.deletePhotoFromPath(photo.getFileName(), THUMBNAIL_FOLDER_NAME, album_id);
                photoService.delete(photo);
            }
            albumService.deleteAlbum(album);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

}
