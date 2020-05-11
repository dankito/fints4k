package net.dankito.banking.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class BankIconFinderTest {

    private val underTest = object : BankIconFinder() {

        fun findBestUrlForBankPublic(bankName: String, urlCandidates: List<String>): String? {
            return super.findBestUrlForBank(bankName, urlCandidates)
        }

    }


    @Test
    fun findBestUrlForBank_BerlinerSparkasse() {

        // when
        val result = underTest.findBestUrlForBankPublic("Landesbank Berlin - Berliner Sparkasse", listOf(
            "https://www.lbb.de",
            "https://www.berliner-sparkasse.de",
            "https://www.berliner-sparkasse.de/de/home/toolbar/impressum.html",
            "https://onlinestreet.de/banken/bank/2745",
            "https://www.berliner-sparkasse.de/en/home.html",
            "https://www.berlin.de/ba-charlottenburg-wilmersdorf/ueber-den-bezirk/wirtschaft/banken/...",
            "https://www.lbb.de/landesbank/de/10_Veroeffentlichungen/10_finanzberichte/015_LBB/LBB...",
            "https://www.bankleitzahl-bic.de/landesbank-berlin-berliner-sparkasse-berlin-blz-10050000",
            "https://www.berliner-sparkasse.de/de/home/privatkunden/online-mobile-banking.html",
            "https://www.berliner-sparkasse.de/de/home/privatkunden/girokonto/kontopfaendung.html"
        ))

        // then
        assertThat(result).isEqualTo("https://www.berliner-sparkasse.de")
    }

    @Test
    fun findBestUrlForBank_Postbank() {

        // when
        val result = underTest.findBestUrlForBankPublic("Postbank Ndl der DB Privat- und Firmenkundenbank", listOf(
            "https://www.postbank.de/privatkunden/kontakt.html",
            "https://onlinestreet.de/banken/bank/538",
            "https://www.zinsen-berechnen.de/.../bank/postbank-ndl-der-db-privat-und-firmenkundenbank",
            "https://www.bankleitzahl-finden.de/Postbank",
            "https://www.postbank.de/firmenkunden",
            "https://antworten.postbank.de/frage/wie-lautet-die-adresse-der-pfaendungsabteilung...",
            "https://www.db.com/ir/de/db-pfk-postbank-finanzpublikationen.htm",
            "https://www.fb.postbank.de/iisenbart/unternehmen/Impressum.php"
        ))


        // then
        assertThat(result).isEqualTo("https://www.postbank.de")
    }

    @Test
    fun findBestUrlForBank_Commerzbank() {

        // when
        val result = underTest.findBestUrlForBankPublic("Commerzbank, Filiale Berlin 1", listOf(
            "https://onlinestreet.de/banken/bank/24463",
            "https://www.commerzbank.de/filialen/de/filial-uebersicht.html",
            "https://filialsuche.commerzbank.de/de/city/Berlin",
            "https://www.meinprospekt.de/berlin/filialen/commerzbank-de",
            "https://www.commerzbank.de/de/hauptnavigation/presse/mediathek/bilddaten/filialen/...",
            "https://www.kaufda.de/Filialen/Berlin/Commerzbank/v-r841",
            "https://www.bankleitzahl-bic.de/commerzbank-filiale-berlin-1-berlin-blz-10040000"
        ))


        // then
        assertThat(result).isEqualTo("https://www.commerzbank.de")
    }

    @Test
    fun findBestUrlForBank_SpardaBankBerlin() {

        // when
        val result = underTest.findBestUrlForBankPublic("Sparda-Bank Berlin", listOf(
            "https://www.sparda-b.de",
            "https://www.meinprospekt.de/berlin/filialen/sparda-bank",
            "https://www.sparda.de/online-service-banking-app-berlin",
            "https://www.sparda-n.de/online-banking-jetzt-online-banking-freischalten",
            "https://www.berlin.de/special/finanzen-und-recht/adressen/bank/spardabank-berliner...",
            "https://www.berlin.de/special/finanzen-und-recht/adressen/bank/spardabank...",
            "https://genostore.de/SBB/online-banking",
            "https://www.sparda-west.de/online-banking-ihr-online-banking",
            "https://www.sparda.de/genossenschaftsbank-gute-gruende"
        ))


        // then
        assertThat(result).isEqualTo("https://www.sparda-b.de")
    }

    @Test
    fun findBestUrlForBank_Dexia() {

        underTest.findBankWebsite("Dexia Kommunalbank Deutschland - DPB")

        // when
        val result = underTest.findBestUrlForBankPublic("Dexia Kommunalbank Deutschland - DPB", listOf(
            "https://www.boomle.com/dexia-kommunalbank",
            "https://www.helaba.com/de/informationen-fuer/medien-und-oeffentlichkeit/news/meldungen/...",
            "https://www.wiwo.de/unternehmen/banken/352-millionen-euro-helaba-kauft-dexia...",
            "https://www.dexia.com/sites/default/files/2020-01/DSA%20FHalf-yearly%20FReport%20F2019%20FEN.pdf",
            "https://www.dexia.com/sites/default/files/2019-12/DSA%20FAnnual%20FReport%20F2018%20FEN_0.pdf",
            "https://www.online-handelsregister.de/.../D/Dexia+Hypothekenbank+Berlin+AG/3102677"
        ))


        // then
        assertThat(result).isEqualTo("https://www.dexia.com")
    }

    @Test
    fun findBestUrlForBank_BhfBank() {

        // when
        val result = underTest.findBestUrlForBankPublic("BHF-BANK", listOf(
            "https://www.bhf-bank.com",
            "https://www.oddo-bhf.com/de",
            "https://www.oddo-bhf.com/#!identite/de",
            "https://www.bv-activebanking.de/onlinebanking-bhf/sessionEnded.jsp",
            "https://www.handelsblatt.com/themen/bhf-bank",
            "https://www.faz.net/aktuell/finanzen/thema/bhf-bank",
            "https://www.kununu.com/de/oddo-bhf",
            "https://www.wallstreet-online.de/thema/bhf-bank"
        ))


        // then
        assertThat(result).isEqualTo("https://www.bhf-bank.com")
    }

    @Test
    fun findBestUrlForBank_BankhausLöbbecke() {

        // when
        val result = underTest.findBestUrlForBankPublic("Bankhaus Löbbecke", listOf(
            "https://www.mmwarburg.de",
            "https://www.berlin.de/special/finanzen-und-recht/adressen/bank/bankhaus-loebbecke-4f...",
            "https://www.wallstreet-online.de/thema/bankhaus-loebbecke",
            "https://www.mmwarburg.de/de/bankhaus/historie/ehemalige-tochterbanken",
            "https://de.kompass.com/c/bankhaus-lobbecke-ag/de665396",
            "https://www.fuchsbriefe.de/ratings/vermoegensmanagement/bankhaus-loebbecke-ag-vor..."
        ))


        // then
        assertThat(result).isEqualTo("https://www.mmwarburg.de")
    }

    @Test
    fun findBestUrlForBank_EurocityBank() {

        // when
        val result = underTest.findBestUrlForBankPublic("Eurocity Bank Gf GAA", listOf(
            "https://www.eurocitybank.de",
            "https://www.eurocitybank.de/?q=de/Festgeld"
        ))


        // then
        assertThat(result).isEqualTo("https://www.eurocitybank.de")
    }

    @Test
    fun findBestUrlForBank_BankFürKircheUndDiakonie() {

        // when
        val result = underTest.findBestUrlForBankPublic("Bank für Kirche und Diakonie - KD-Bank Gf Sonder-BLZ", listOf(
            "https://www.kd-bank.de",
            "https://www.kd-bank.de/privatkunden.html",
            "https://www.kd-bank.de/service/impressum.html"
        ))


        // then
        assertThat(result).isEqualTo("https://www.kd-bank.de")
    }

    @Test
    fun findBestUrlForBank_PsdBankKiel() {

        // when
        val result = underTest.findBestUrlForBankPublic("PSD Bank Kiel (Gf P2)", listOf(
            "https://www.psd-kiel.de",
            "https://www.onlinebanking-psd-kiel.de/banking-private/entry",
            "https://www.kreditbanken.de/21090900.html"
        ))

        // then
        assertThat(result).isEqualTo("https://www.psd-kiel.de")
    }

    @Test
    fun findBestUrlForBank_VrBankFlensburgSchleswig() {

        // when
        val result = underTest.findBestUrlForBankPublic("VR Bank Flensburg-Schleswig -alt-", listOf(
            "https://www.vrbanknord.de/banking-private/entry",
            "https://www.vrbanknord.de/wir-fuer-sie/filialen-ansprechpartner/filialen/uebersicht...",
            "https://sh.vr.de/privatkunden/service/kontakt.html",
            "https://www.vrbanknord-immo.de/kontakt/ihre-ansprechpartner",
            "https://sh.vr.de",
            "https://www.unser-flensburg.de/flensburg/bankensparkassen/vrflensburgschleswig",
            "https://www.kununu.com/de/vr-bank-flensburg-schleswig-eg",
            "https://www.meine-vrbank.de"
        ))

        // then
        assertThat(result).isEqualTo("https://www.vrbanknord.de")
    }

    @Test
    fun findBankWebsite_VrBankLichtenfelsEbern() {

        // when
        val result = underTest.findBankWebsite("VR-Bank Lichtenfels-Ebern (Gf P2)")

        // then
        assertThat(result).isEqualTo("https://www.vr-lif-ebn.de")
    }

    @Test
    fun findBankWebsite_PsdBankKoblenz() {

        // when
        val result = underTest.findBankWebsite("PSD Bank Koblenz (Gf P2)")

        // then
        assertThat(result).isEqualTo("https://www.psd-koblenz.de")
    }

    @Test
    fun findBankWebsite_VrBankLandauMengkofen() {

        // when
        val result = underTest.findBankWebsite("VR-Bank Landau-Mengkofen (Gf P2)")

        // then
        assertThat(result).isEqualTo("https://www.vrbanklm.de")
    }

    @Test
    fun findBankWebsite_InvestitionsbankBerlin() {

        // when
        val result = underTest.findBankWebsite("Investitionsbank Berlin")

        // then
        assertThat(result).isEqualTo("https://www.ibb.de")
    }

    @Test
    fun findBankWebsite_DexiaKommunalbankDeutschland() {

        // when
        val result = underTest.findBankWebsite("Dexia Kommunalbank Deutschland - DPB")

        // then
        assertThat(result).isEqualTo("https://www.dexia.com")
    }

}