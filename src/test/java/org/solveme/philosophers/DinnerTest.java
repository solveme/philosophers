package org.solveme.philosophers;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.solveme.philosophers.Identity.*;


class DinnerTest {

    private TestingDinner dinner;

    @BeforeEach
    void setUp() {
        DinnerApp.Settings settings = DinnerApp.Settings.builder()
                .seatCount(4)
                .build();

        dinner = new TestingDinner(settings);
        dinner.init();
    }

    /**
     * See layout diagram for explanation
     */
    static Stream<NeighbourhoodTestCase> neighbourhoodTestCases() {
        return Stream.of(
                NeighbourhoodTestCase.from(PLATO, ARISTOTLE, DIOGEN),
                NeighbourhoodTestCase.from(SOCRATES, PLATO, ARISTOTLE),
                NeighbourhoodTestCase.from(DIOGEN, SOCRATES, PLATO),
                NeighbourhoodTestCase.from(ARISTOTLE, DIOGEN, SOCRATES)
        );
    }

    /**
     * LN LF O RF RN
     */
    @ParameterizedTest
    @MethodSource("neighbourhoodTestCases")
    void assertNeighbourResolution(NeighbourhoodTestCase testCase) {
        Identity originIdentity = testCase.getOrigin();

        TestingDinner.TestingPhilosopher origin = dinner.getPhilosopherBySeatId(originIdentity.getSeatId());
        TestingDinner.TestingPhilosopher leftNeighbour = origin.getLeftNeighbour();
        TestingDinner.TestingPhilosopher rightNeighbour = origin.getRightNeighbour();

        // when
        assertEquals(testCase.getLeftNeighbourhood(), leftNeighbour.getIdentity());
        assertEquals(testCase.getRightNeighbourhood(), rightNeighbour.getIdentity());

        assertEquals(leftNeighbour.getRightFork(), origin.getLeftFork());
        assertEquals(origin.getRightFork(), rightNeighbour.getLeftFork());
    }


    @Accessors(chain = true)
    @Setter(value = AccessLevel.PRIVATE)
    @Getter
    static class NeighbourhoodTestCase {

        private Identity origin;
        private Identity leftNeighbourhood;
        private Identity rightNeighbourhood;

        public static NeighbourhoodTestCase from(Identity left, Identity origin, Identity right) {
            return new NeighbourhoodTestCase()
                    .setOrigin(origin)
                    .setLeftNeighbourhood(left)
                    .setRightNeighbourhood(right);
        }

    }

}