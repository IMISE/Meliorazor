package eu.brunsch.terminology.annotator.questionnaire.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Groups options for configuring the annotation process for a single project.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Embeddable
public class QuestionnaireOptions {
    /**
     * Minimum number of positive reviews required
     * for am annotation to be considered accepted.
     */
    private Integer reqPosReviews;

    /**
     * Ratio of number of positive reviews to number of all reviews
     * for an annotation to be considered accepted.
     *
     * In percent, so times 100.
     */
    private Float reqPosReviewRatio;

    /**
     * Minimum number of negative reviews required
     * for am annotation to be considered rejected.
     */
    private Integer reqNegReviews;

    /**
     * Ratio of number of negative reviews to number of all reviews
     * for an annotation to be considered rejected.
     *
     * In percent, so times 100.
     */
    private Float reqNegReviewRatio;

    /**
     * Check if an annotation has enough comments accepting it
     * so that it's considered accepted.
     *
     * @param acceptions number of accepting votes
     * @param total total number of relevant votes (pos + neg)
     * @return Does the annotation fulfill the requirements
     *  to be considered accepted?
     */
    public boolean checkAccepted(final int acceptions, final int total) {
        return checkCriteria(
            acceptions, total, reqPosReviews, reqPosReviewRatio);
    }

    /**
     * Check if an annotation has enough comments rejecting it
     * so that it's considered rejected.
     *
     * @param rejections number of rejecting votes
     * @param total total number of relevant votes (pos + neg)
     * @return Does the annotation fulfill the requirements
     *  to be considered rejected?
     */
    public boolean checkRejected(final int rejections, final int total) {
        return checkCriteria(
            rejections, total, reqNegReviews, reqNegReviewRatio);
    }

    /**
     * Check if count >= minCount and count/total >= minRatio.
     * @param count number of entities fulfilling critereon
     * @param total total count of entities
     * @param minCount treshold, will return false if minCount > count
     * @param minRatio treshold, will return false if minRatio > count/total
     * @return count >= minCount && count/total >= minRatio?
     */
    private static boolean checkCriteria(
        final int count,
        final int total,
        final Integer minCount,
        final Float minRatio
    ) {
        boolean result = false;
        if (minCount != null) {
            result = result && minCount <= count;
        }
        if (minRatio != null) {
            result = result && Float.compare(minRatio, ((float) count * 100) / ((float) total)) >= 0;
        }
        return result;
    }

}
