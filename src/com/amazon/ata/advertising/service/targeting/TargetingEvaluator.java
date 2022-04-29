package com.amazon.ata.advertising.service.targeting;

import com.amazon.ata.advertising.service.model.RequestContext;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicate;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicateResult;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Evaluates TargetingPredicates for a given RequestContext.
 */
public class TargetingEvaluator {
    public static final boolean IMPLEMENTED_STREAMS = true;
    public static final boolean IMPLEMENTED_CONCURRENCY = true;
    private final RequestContext requestContext;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Creates an evaluator for targeting predicates.
     * @param requestContext Context that can be used to evaluate the predicates.
     */
    public TargetingEvaluator(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    /**
     * Evaluate a TargetingGroup to determine if all of its TargetingPredicates are TRUE or not for the given
     * RequestContext.
     * @param targetingGroup Targeting group for an advertisement, including TargetingPredicates.
     * @return TRUE if all of the TargetingPredicates evaluate to TRUE against the RequestContext, FALSE otherwise.
     */
    public TargetingPredicateResult evaluate(TargetingGroup targetingGroup) {

//        List<TargetingPredicate> targetingPredicates = targetingGroup.getTargetingPredicates();
//        boolean allTruePredicates = true;
//        for (TargetingPredicate predicate : targetingPredicates) {
//            TargetingPredicateResult predicateResult = predicate.evaluate(requestContext);
//            if (!predicateResult.isTrue()) {
//                allTruePredicates = false;
//                break;
//            }
//        }
//
//        return allTruePredicates ? TargetingPredicateResult.TRUE :
//                                   TargetingPredicateResult.FALSE;


        List<Future<TargetingPredicateResult>> futureStream = targetingGroup.getTargetingPredicates().stream()
                .map(targetingPredicate -> executorService.submit(() ->  targetingPredicate.evaluate(requestContext)))
                .collect(Collectors.toList());

        executorService.shutdown();


            for (Future<TargetingPredicateResult> targetingPredicateResultFuture : futureStream) {
                try {

                TargetingPredicateResult targetingPredicateResult = targetingPredicateResultFuture.get();

                    if (!targetingPredicateResult.isTrue()) {
                        return  TargetingPredicateResult.FALSE;
                    }
                } catch (InterruptedException | ExecutionException e){
                    e.printStackTrace();
                }

            }

            return TargetingPredicateResult.TRUE;
//                .allMatch(targetingPredicateResult ->
//                        targetingPredicateResult.isTrue())
//                        ? TargetingPredicateResult.TRUE : TargetingPredicateResult.FALSE;
    }

}
