package cz.psgs.SpringRestDemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cz.psgs.SpringRestDemo.model.Album;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long>{

}
