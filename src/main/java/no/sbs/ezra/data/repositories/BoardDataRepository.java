package no.sbs.ezra.data.repositories;

import no.sbs.ezra.data.BoardData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


import java.util.List;


public interface BoardDataRepository extends CrudRepository<BoardData, Integer> {

    @Query("SELECT b FROM BoardData b WHERE b.name like %?1%")
    List<BoardData> search(String search);

    BoardData findByName(String boardDataName);

}
