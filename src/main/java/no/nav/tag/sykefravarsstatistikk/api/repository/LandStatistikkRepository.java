package no.nav.tag.sykefravarsstatistikk.api.repository;

import no.nav.tag.sykefravarsstatistikk.api.domain.stats.LandStatistikk;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface LandStatistikkRepository extends CrudRepository<LandStatistikk, Integer>  {

    @Query("SELECT * FROM LAND_STATISTIKK_SYKEFRAVAR")
    Collection<LandStatistikk> findAll();

    @Query("SELECT * FROM LAND_STATISTIKK_SYKEFRAVAR WHERE arstall = :arstall")
    Collection<LandStatistikk> findForArstall(@Param(value = "arstall") int arstall);

}
