package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;

import java.math.BigDecimal;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;

public class EksporteringServiceTestUtils {

    // Data for testing & Utilities
    public static ÅrstallOgKvartal __2020_4 = new ÅrstallOgKvartal(2020, 4);
    public static ÅrstallOgKvartal __2020_2 = new ÅrstallOgKvartal(2020, 2);
    public static ÅrstallOgKvartal __2021_1 = new ÅrstallOgKvartal(2021, 1);
    public static ÅrstallOgKvartal __2021_2 = new ÅrstallOgKvartal(2021, 2);
    public static Orgnr ORGNR_VIRKSOMHET_1 = new Orgnr("987654321");
    public static Orgnr ORGNR_VIRKSOMHET_2 = new Orgnr("912345678");

    public static VirksomhetMetadata virksomhet1Metadata_2020_4 = new VirksomhetMetadata(
            ORGNR_VIRKSOMHET_1,
            "Virksomhet 1",
            RECTYPE_FOR_VIRKSOMHET,
            "1",
            "11",
            __2020_4
    );

    public static VirksomhetMetadata virksomhet2Metadata_2020_4 = new VirksomhetMetadata(
            ORGNR_VIRKSOMHET_2,
            "Virksomhet 2",
            RECTYPE_FOR_VIRKSOMHET,
            "2",
            "22",
            __2020_4
    );

    public static VirksomhetMetadata virksomhet1Metadata_2021_1 = new VirksomhetMetadata(
            ORGNR_VIRKSOMHET_1,
            "Virksomhet 1",
            RECTYPE_FOR_VIRKSOMHET,
            "1",
            "11",
            __2021_1
    );

    public static VirksomhetMetadata virksomhet1Metadata_2021_2 = new VirksomhetMetadata(
            ORGNR_VIRKSOMHET_1,
            "Virksomhet 1",
            RECTYPE_FOR_VIRKSOMHET,
            "1",
            "11",
            __2021_2
    );

    public static SykefraværsstatistikkVirksomhetUtenVarighet byggSykefraværsstatistikkVirksomhet(
            VirksomhetMetadata virksomhetMetadata
    ) {
        return byggSykefraværsstatistikkVirksomhet(
                virksomhetMetadata,
                156,
                3678,
                188000
        );
    }

    public static SykefraværsstatistikkVirksomhetUtenVarighet byggSykefraværsstatistikkVirksomhet(
            VirksomhetMetadata virksomhetMetadata,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        return new SykefraværsstatistikkVirksomhetUtenVarighet(
                virksomhetMetadata.getÅrstall(),
                virksomhetMetadata.getKvartal(),
                virksomhetMetadata.getOrgnr(),
                antallPersoner,
                new BigDecimal(tapteDagsverk),
                new BigDecimal(muligeDagsverk)
        );
    }

    public static VirksomhetSykefravær tomVirksomhetSykefravær(VirksomhetMetadata virksomhetMetadata) {
        return new VirksomhetSykefravær(
                virksomhetMetadata.getOrgnr(),
                virksomhetMetadata.getNavn(),
                new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
                null,
                null,
                0
        );
    }

    public static SykefraværsstatistikkNæring byggSykefraværStatistikkNæring(
            VirksomhetMetadata virksomhetMetadata,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        return new SykefraværsstatistikkNæring(
                virksomhetMetadata.getÅrstall(),
                virksomhetMetadata.getKvartal(),
                virksomhetMetadata.getNæring(),
                antallPersoner,
                new BigDecimal(tapteDagsverk),
                new BigDecimal(muligeDagsverk)
        );
    }

    public static SykefraværsstatistikkNæring byggSykefraværStatistikkNæring(VirksomhetMetadata virksomhetMetadata) {
        return new SykefraværsstatistikkNæring(
                virksomhetMetadata.getÅrstall(),
                virksomhetMetadata.getKvartal(),
                virksomhetMetadata.getNæring(),
                156,
                new BigDecimal(3678),
                new BigDecimal(188000)
        );
    }
public static SykefraværsstatistikkNæring5Siffer byggSykefraværStatistikkNæring5Siffer(VirksomhetMetadata virksomhetMetadata,
String næringskode5Siffer) {
        return new SykefraværsstatistikkNæring5Siffer(
                virksomhetMetadata.getÅrstall(),
                virksomhetMetadata.getKvartal(),
                næringskode5Siffer,
                100,
                new BigDecimal(250),
                new BigDecimal(25000)
        );
    }

    public static SykefraværsstatistikkSektor byggSykefraværStatistikkSektor(
            VirksomhetMetadata virksomhetMetadata,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        return new SykefraværsstatistikkSektor(
                virksomhetMetadata.getÅrstall(),
                virksomhetMetadata.getKvartal(),
                virksomhetMetadata.getSektor(),
                antallPersoner,
                new BigDecimal(tapteDagsverk),
                new BigDecimal(muligeDagsverk)
        );
    }

    public static SykefraværsstatistikkSektor byggSykefraværStatistikkSektor(VirksomhetMetadata virksomhetMetadata) {
        return new SykefraværsstatistikkSektor(
                virksomhetMetadata.getÅrstall(),
                virksomhetMetadata.getKvartal(),
                virksomhetMetadata.getSektor(),
                156,
                new BigDecimal(3678),
                new BigDecimal(188000)
        );
    }

    public static VirksomhetSykefravær byggVirksomhetSykefravær(
            VirksomhetMetadata virksomhetMetadata,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        return new VirksomhetSykefravær(
                virksomhetMetadata.getOrgnr(),
                virksomhetMetadata.getNavn(),
                new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
                new BigDecimal(tapteDagsverk),
                new BigDecimal(muligeDagsverk),
                antallPersoner
        );
    }

    public static VirksomhetSykefravær byggVirksomhetSykefravær(VirksomhetMetadata virksomhetMetadata) {
        return new VirksomhetSykefravær(
                virksomhetMetadata.getOrgnr(),
                virksomhetMetadata.getNavn(),
                new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
                new BigDecimal(3678),
                new BigDecimal(188000),
                156
        );
    }

    public static SykefraværsstatistikkVirksomhetUtenVarighet sykefraværsstatistikkVirksomhet =
            new SykefraværsstatistikkVirksomhetUtenVarighet(
                    __2020_2.getÅrstall(),
                    __2020_2.getKvartal(),
                    "987654321",
                    6,
                    new BigDecimal(10),
                    new BigDecimal(500)
            );

    public static VirksomhetEksportPerKvartal virksomhetEksportPerKvartal = new VirksomhetEksportPerKvartal(
            new Orgnr("987654321"),
            __2020_2,
            false
    );
    public static VirksomhetMetadata virksomhetMetadata = new VirksomhetMetadata(
            new Orgnr("987654321"),
            "Virksomhet 1",
            "2",
            "1",
            "11",
            __2020_2
    );
    public static SykefraværsstatistikkLand sykefraværsstatistikkLand = new SykefraværsstatistikkLand(
            __2020_2.getÅrstall(),
            __2020_2.getKvartal(),
            2500000,
            new BigDecimal(10000000),
            new BigDecimal(500000000)
    );
    public static SykefraværsstatistikkSektor sykefraværsstatistikkSektor = new SykefraværsstatistikkSektor(
            __2020_2.getÅrstall(),
            __2020_2.getKvartal(),
            "1",
            33000,
            new BigDecimal(1340),
            new BigDecimal(88000)
    );
    public static SykefraværsstatistikkNæring sykefraværsstatistikkNæring = new SykefraværsstatistikkNæring(
            __2020_2.getÅrstall(),
            __2020_2.getKvartal(),
            "11",
            150,
            new BigDecimal(100),
            new BigDecimal(5000)
    );
    public static SykefraværsstatistikkNæring5Siffer sykefraværsstatistikkNæring5Siffer = new SykefraværsstatistikkNæring5Siffer(
            __2020_2.getÅrstall(),
            __2020_2.getKvartal(),
            "11000",
            1250,
            new BigDecimal(40),
            new BigDecimal(4000)
    );
}
