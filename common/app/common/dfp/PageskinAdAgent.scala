package common.dfp

import common.Edition
import com.gu.commercial.display.AdTargetParam.toMap
import com.gu.commercial.display.{AdTargetParamValue, MultipleValues}
import model.MetaData

trait PageskinAdAgent {

  protected val environmentIsProd: Boolean

  protected def pageSkinSponsorships: Seq[PageSkinSponsorship]

  // There are two forms of pageskins:
  // - pageskins that target through ad unit (for pressed fronts)
  // - pageskins that target through a keyword (for index page fronts)
  private def findSponsorships(adUnitPath: String, metaData: MetaData, edition: Edition): Seq[PageSkinSponsorship] = {

    val candidates = pageSkinSponsorships filter { sponsorship =>
      sponsorship.editions.contains(edition) && !sponsorship.isR2Only }

    if (metaData.isPressedPage) {
      if (PageSkin.isValidAdUnit(adUnitPath)) {
        candidates filter { sponsorship => sponsorship.adUnits.exists(adUnitPath.endsWith) }
      } else Seq.empty
    } else {
      val targetingMap = toMap(metaData.commercial.map(_.adTargeting(edition)).getOrElse(Set.empty))

      // Little DRY helper
      val targetingMapValues = ((map: Map[String, AdTargetParamValue], key: String) =>
        map.get(key) match {
          case Some(values: MultipleValues) => values.values.toSeq
          case _ => Seq.empty
        }
      )

      val keywordTargeting = targetingMapValues( targetingMap , "k" )
      val serieTargeting = targetingMapValues( targetingMap , "se" )

      candidates filter { sponsorship => {
                           // Two ways of matching:
                           // - either the right keyword is there
                           sponsorship.keywords.intersect(keywordTargeting).nonEmpty ||
                           // - or the right series
                           sponsorship.series.intersect(serieTargeting).nonEmpty
                         }}
    }
  }

  // The ad unit is considered to have a page skin if it has a corresponding sponsorship.
  // If the sponsorship is an adTest, it is only considered outside of production.
  def hasPageSkin(fullAdUnitPath: String, metaData: MetaData, edition: Edition): Boolean = {
    if (metaData.isFront) {
      findSponsorships(fullAdUnitPath, metaData, edition) exists (sponsorship =>
        !(environmentIsProd && sponsorship.targetsAdTest))
    } else false
  }

  // True if there is any candidate sponsorship for this ad unit. Used to decide when to render the out-of-page ad slot.
  def hasPageSkinOrAdTestPageSkin(fullAdUnitPath: String, metaData: MetaData, edition: Edition): Boolean = {
    if (metaData.isFront) {
      findSponsorships(fullAdUnitPath, metaData, edition).nonEmpty
    } else false
  }
}
