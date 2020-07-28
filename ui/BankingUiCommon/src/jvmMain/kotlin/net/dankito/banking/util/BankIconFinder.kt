package net.dankito.banking.util

import net.dankito.utils.favicon.FaviconComparator
import net.dankito.utils.favicon.FaviconFinder
import net.dankito.utils.web.client.OkHttpWebClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.regex.Pattern


open class BankIconFinder : IBankIconFinder {

    companion object {

        const val SearchBankWebsiteBaseUrlQwant = "https://lite.qwant.com/?l=de&t=mobile&q="

        const val SearchBankWebsiteBaseUrlEcosia = "https://www.ecosia.org/search?q="

        const val SearchBankWebsiteBaseUrlDuckDuckGo = "https://duckduckgo.com/html/?q="


        val ReplaceGfRegex = Pattern.compile(" \\(Gf [\\w]+\\)").toRegex()


        private val log = LoggerFactory.getLogger(BankIconFinder::class.java)

    }


    protected val webClient = OkHttpWebClient()

    protected val faviconFinder = FaviconFinder(webClient)

    protected val faviconComparator = FaviconComparator(webClient)


    override fun findIconForBank(bankName: String, prefSize: Int): String? {
        findBankWebsite(bankName)?.let { bankUrl ->
            webClient.get(bankUrl).body?.let { bankHomepageResponse ->
                val favicons = faviconFinder.extractFavicons(Jsoup.parse(bankHomepageResponse), bankUrl)

                faviconComparator.getBestIcon(favicons, prefSize, prefSize + 32, true)?.let { prefFavicon ->
                    return prefFavicon.url
                }

                return faviconComparator.getBestIcon(favicons, 16)?.url
            }
        }

        return null
    }


    open fun findBankWebsite(bankName: String): String? {
        try {
            val adjustedBankName = bankName.replace("-alt-", "").replace(ReplaceGfRegex, "")

            findBankWebsiteWithQwant(adjustedBankName)?.let { return it }

            log.warn("Could not find bank website with Qwant for '$bankName'")

            findBankWebsiteWithEcosia(adjustedBankName)?.let { return it }

            log.warn("Could not find bank website with Ecosia for '$bankName'")

            findBankWebsiteWithDuckDuckGo(adjustedBankName)?.let { return it }
        } catch (e: Exception) {
            log.error("Could not find website for bank '$bankName'", e)
        }

        return null
    }

    protected open fun findBankWebsiteWithQwant(bankName: String): String? {
        try {
            return findBankWebsite(bankName, SearchBankWebsiteBaseUrlQwant) { searchResponseDoc ->
                searchResponseDoc.select(".url")
                    .filter { it.selectFirst("span") == null }.map { it.text() }
            }
        } catch (e: Exception) {
            log.error("Could not find website for bank '$bankName' with Qwant", e)
        }

        return null
    }

    protected open fun findBankWebsiteWithEcosia(bankName: String): String? {
        try {
            return findBankWebsite(bankName, SearchBankWebsiteBaseUrlEcosia) { searchResponseDoc ->
                searchResponseDoc.select(".js-result-url").map { it.attr("href") }
            }
        } catch (e: Exception) {
            log.error("Could not find website for bank '$bankName' with DuckDuckGo", e)
        }

        return null
    }

    protected open fun findBankWebsiteWithDuckDuckGo(bankName: String): String? {
        try {
            return findBankWebsite(bankName, SearchBankWebsiteBaseUrlDuckDuckGo) { searchResponseDoc ->
                searchResponseDoc.select(".result__url").map { it.attr("href") }
            }
        } catch (e: Exception) {
            log.error("Could not find website for bank '$bankName' with DuckDuckGo", e)
        }

        return null
    }

    protected open fun findBankWebsite(bankName: String, searchBaseUrl: String, extractUrls: (Document) -> List<String>): String? {
        val encodedBankName = bankName.replace(" ", "+")

        val exactSearchUrl = searchBaseUrl + "\"" + encodedBankName + "\""
        getSearchResultForBank(exactSearchUrl)?.let { searchResponseDocument ->
            findBestUrlForBank(bankName, extractUrls(searchResponseDocument))?.let {  bestUrl ->
                return bestUrl
            }
        }


        val searchUrl = searchBaseUrl + encodedBankName
        getSearchResultForBank(searchUrl)?.let { searchResponseDocument ->
            return findBestUrlForBank(bankName, extractUrls(searchResponseDocument))
        }


        return null
    }

    protected open fun getSearchResultForBank(searchUrl: String): Document? {
        val response = webClient.get(searchUrl)

        response.body?.let { responseBody ->
            return Jsoup.parse(responseBody)
        }

        return null
    }


    protected open fun findBestUrlForBank(bankName: String, unmappedUrls: List<String>): String? {
        val urlCandidates = getUrlCandidates(unmappedUrls)
        val urlCandidatesWithoutUnlikely = urlCandidates.filterNot { isUnlikelyBankUrl(bankName, it) }

        val urlForBank = findUrlThatContainsBankName(bankName, urlCandidatesWithoutUnlikely)

        // cut off stuff like 'filalsuche' etc., they most like don't contain as many favicons as main page
        return getMainPageForBankUrl(urlForBank, urlCandidatesWithoutUnlikely) ?: urlForBank
    }

    protected open fun getUrlCandidates(urls: List<String?>): List<String> {
        return urls.mapNotNull { fixUrl(it) }
    }

    protected open fun fixUrl(url: String?): String? {
        if (url.isNullOrBlank() == false) {
            val urlEncoded = url.replace(" ", "%20F")

            if (urlEncoded.startsWith("http")) {
                return urlEncoded
            }
            else {
                return "https://" + urlEncoded
            }
        }

        return null
    }

    protected open fun findUrlThatContainsBankName(bankName: String, urlCandidates: List<String>): String? {
        val bankNameParts = bankName.replace(",", "")
            .replace("-", " ") // to find 'Sparda-Bank' in 'sparda.de'
            .replace("ä", "ae", true).replace("ö", "oe", true).replace("ü", "ue", true)
            .split(" ")
            .filter { it.isNullOrBlank() == false }
        val urlsContainsPartsOfBankName = mutableMapOf<Int, MutableList<String>>()

        urlCandidates.forEach { urlCandidate ->
            findBankNameInUrlHost(urlCandidate, bankNameParts)?.let { containingCountParts ->
                if (urlsContainsPartsOfBankName.containsKey(containingCountParts) == false) {
                    urlsContainsPartsOfBankName.put(containingCountParts, mutableListOf(urlCandidate))
                }
                else {
                    urlsContainsPartsOfBankName[containingCountParts]!!.add(urlCandidate)
                }
            }
        }

        urlsContainsPartsOfBankName.keys.max()?.let { countMostMatches ->
            val urisWithMostMatches = urlsContainsPartsOfBankName[countMostMatches]

            return urisWithMostMatches?.firstOrNull()
        }

        return null
    }

    protected open fun findBankNameInUrlHost(urlCandidate: String, bankNameParts: List<String>): Int? {
        try {
            val candidateUri = URI.create(urlCandidate.replace("onlinebanking-", ""))
            val candidateHost = candidateUri.host

            return bankNameParts.filter { part -> candidateHost.contains(part, true) }.size
        } catch (e: Exception) {
            log.warn("Could not find host of url '$urlCandidate' in bank name $bankNameParts'", e)
        }

        return null
    }

    protected open fun getMainPageForBankUrl(urlForBank: String?, urlCandidates: List<String>): String? {
        try {
            urlForBank?.let {
                if (isHomePage(urlForBank)) {
                    return urlForBank
                }

                val bankUri = URI.create(urlForBank)
                val bankUriHost = bankUri.host

                urlCandidates.forEach { candidateUrl ->
                    val candidateUri = URI.create(candidateUrl)

                    if (candidateUri.host == bankUriHost && isHomePage(candidateUrl)) {
                        return candidateUrl
                    }
                }
            }
        } catch (e: Exception) {
            log.warn("Could not find main page for bank url '$urlForBank'", e)
        }

        try {
            if (urlForBank != null) {
                val bankUri = URI.create(urlForBank)

                return bankUri.scheme + "://" + bankUri.host
            }
        } catch (e: Exception) {
            log.error("Could get main page for bank url '$urlForBank'", e)
        }

        return null
    }

    protected open fun isHomePage(url: String): Boolean {
        try {
            val uri = URI.create(url)

            if (uri.path.isNullOrBlank() && uri.host.startsWith("www.")) {
                return true
            }
        } catch (e: Exception) {
            log.warn("Could not check if '$url' is url of domain's home page", e)
        }

        return false
    }

    protected open fun isUnlikelyBankUrl(bankName: String, urlCandidate: String): Boolean {
        return urlCandidate.contains("meinprospekt.de/")
                || urlCandidate.contains("onlinestreet.de/")
                || urlCandidate.contains("iban-blz.de/")
                || urlCandidate.contains("bankleitzahlen.ws/")
                || urlCandidate.contains("bankleitzahl-finden.de/")
                || urlCandidate.contains("bankleitzahl-bic.de/")
                || urlCandidate.contains("bankleitzahlensuche.org/")
                || urlCandidate.contains("bankleitzahlensuche.com/")
                || urlCandidate.contains("bankverzeichnis.com")
                || urlCandidate.contains("banksuche.com/")
                || urlCandidate.contains("bank-code.net/")
                || urlCandidate.contains("thebankcodes.com/")
                || urlCandidate.contains("zinsen-berechnen.de/")
                || urlCandidate.contains("kredit-anzeiger.com/")
                || urlCandidate.contains("kreditbanken.de/")
                || urlCandidate.contains("nifox.de/")
                || urlCandidate.contains("wikipedia.org/")
                || urlCandidate.contains("transferwise.com/")
                || urlCandidate.contains("wogibtes.info/")
                || urlCandidate.contains("11880.com/")
                || urlCandidate.contains("kaufda.de/")
                || urlCandidate.contains("boomle.com/")
                || urlCandidate.contains("berlin.de/")
                || urlCandidate.contains("berliner-zeitung.de")
    }

}