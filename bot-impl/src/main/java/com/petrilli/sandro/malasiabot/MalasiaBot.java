package com.petrilli.sandro.malasiabot;

import com.bueno.spi.model.CardToPlay;
import com.bueno.spi.model.GameIntel;
import com.bueno.spi.model.TrucoCard;
import com.bueno.spi.service.BotServiceProvider;

import java.util.List;
import java.util.Optional;

public class MalasiaBot implements BotServiceProvider {

    @Override
    public int getRaiseResponse(GameIntel intel) {

        if (intel.getHandPoints() == 12)
            return -1;

        int response = -1;

        if (MaoGiga(intel) || MaoZapOuCopasEAsAtres(intel) || MaoZapOuCopasEFiguras(intel)) {
            if (intel.getScore() + intel.getHandPoints() >= 12) {
                return 0;
            }
            return 1;
        }

        if (MaoEspadaOuOuroEAsATres(intel) || MaoEspadasOuOurosEFiguras(intel) || MaoMediaComUmaBoaCarta(intel) || MaoComDuasBoasSemManilha(intel)) {
            if (intel.getOpponentScore() + intel.getHandPoints() >= 12) {
                return 1;
            }
            return 0;
        }
        return response;
    }

    @Override
    public CardToPlay chooseCard(GameIntel intel) {

        CardToPlay cardToPlay = CardToPlay.of(DeMaior(intel));

        List<GameIntel.RoundResult> round = intel.getRoundResults();

        TrucoCard vira = intel.getVira();

        if (intel.getOpponentCard().isPresent()) {
            cardToPlay = CardToPlay.of(DeMenorQuePodeGanhar(intel));
        } else {
            if (round.isEmpty()) {
                if (MaoGiga(intel)) {
                    cardToPlay = CardToPlay.of(DeMenor(intel));
                }
                if (MaoZapOuCopasEAsAtres(intel)) {
                    for (TrucoCard card : intel.getCards()) {
                        int cardValue = card.relativeValue(vira);
                        if (cardValue == 7 || cardValue == 8 || cardValue == 9) {
                            return CardToPlay.of(card);
                        }
                    }
                }
                if (MaoZapOuCopasEFiguras(intel)) {
                    cardToPlay = CardToPlay.of(DeMaior(intel));
                }
                if (MaoEspadaOuOuroEAsATres(intel)) {
                    cardToPlay = CardToPlay.of(DeMaior(intel));
                }
            }
        }

        return cardToPlay;
    }

    @Override
    public boolean decideIfRaises(GameIntel intel) {

        if (intel.getOpponentScore() == 11 || intel.getScore() == 11) {
            return false;
        } else {
            List<GameIntel.RoundResult> round = intel.getRoundResults();

            if (!round.isEmpty()) {
                if (MaoGiga(intel) && (round.get(0) == GameIntel.RoundResult.LOST || round.get(0) == GameIntel.RoundResult.DREW)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean getMaoDeOnzeResponse(GameIntel intel) {

        if (MaoGiga(intel)) {
            return true;
        }

        return false;
    }

    private TrucoCard DeMenor(GameIntel intel) {
        TrucoCard deMenor = null;
        for (TrucoCard card : intel.getCards()) {
            if (deMenor == null || card.relativeValue(intel.getVira()) < deMenor.relativeValue(intel.getVira())) {
                deMenor = card;
            }
        }

        return deMenor;
    }

    private TrucoCard DeMaior(GameIntel intel) {
        TrucoCard deMaior = null;
        for (TrucoCard card : intel.getCards()) {
            if (deMaior == null || card.relativeValue(intel.getVira()) > deMaior.relativeValue(intel.getVira())) {
                deMaior = card;
            }
        }
        return deMaior;
    }

    private TrucoCard DeMenorQuePodeGanhar(GameIntel intel) {
        TrucoCard deMenorQuePodeGanhar = null;
        TrucoCard vira = intel.getVira();
        Optional<TrucoCard> opponentCard = intel.getOpponentCard();

        if (opponentCard.isPresent()) {
            TrucoCard opponentCardValue = opponentCard.get();
            for (TrucoCard card : intel.getCards()) {
                int cardValue = card.relativeValue(vira);
                int opponentCardValueRelative = opponentCardValue.relativeValue(vira);
                if (cardValue > opponentCardValueRelative &&
                        (deMenorQuePodeGanhar == null || cardValue < deMenorQuePodeGanhar.relativeValue(vira))) {
                    deMenorQuePodeGanhar = card;
                }
            }
        }

        if (deMenorQuePodeGanhar == null) {
            deMenorQuePodeGanhar = DeMenor(intel);
        }

        return deMenorQuePodeGanhar;
    }

    private boolean MaoGiga(GameIntel intel) {

        TrucoCard vira = intel.getVira();
        List<TrucoCard> cards = intel.getCards();

        int manilhasCount = 0;
        for (TrucoCard card : cards) {
            if (card.relativeValue(vira) > 9) {
                manilhasCount++;
                if (manilhasCount >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean MaoZapOuCopasEFiguras(GameIntel intel) {
        TrucoCard vira = intel.getVira();

        int figurasCount = 0;
        int manilhasCount = 0;

        for (TrucoCard card : intel.getCards()) {
            int cardValue = card.relativeValue(vira);
            if (cardValue == 13 || cardValue == 12) {
                manilhasCount++;
            }
            if (cardValue >= 4 && cardValue <= 6) {
                figurasCount++;
            }
        }
        if (manilhasCount == 1 && figurasCount == 1) {
            return true;
        }
        return false;
    }

    private boolean MaoEspadasOuOurosEFiguras(GameIntel intel) {
        TrucoCard vira = intel.getVira();

        int figurasCount = 0;
        int manilhasCount = 0;

        for (TrucoCard card : intel.getCards()) {
            int cardValue = card.relativeValue(vira);
            if (cardValue == 11 || cardValue == 10) {
                manilhasCount++;
            }
            if (cardValue >= 4 && cardValue <= 6) {
                figurasCount++;
            }
        }
        if (manilhasCount == 1 && figurasCount == 1) {
            return true;
        }
        return false;
    }

    private boolean MaoZapOuCopasEAsAtres(GameIntel intel) {
        TrucoCard vira = intel.getVira();

        int boasCartasCount = 0;
        int manilhasCount = 0;

        for (TrucoCard card : intel.getCards()) {
            int cardValue = card.relativeValue(vira);
            if (cardValue == 13 || cardValue == 12) {
                manilhasCount++;
            }
            if (cardValue >= 7 && cardValue <= 9) {
                boasCartasCount++;
            }
        }
        if (manilhasCount == 1 && boasCartasCount >= 1) {
            return true;
        }
        return false;
    }

    private boolean MaoEspadaOuOuroEAsATres(GameIntel intel) {
        TrucoCard vira = intel.getVira();

        int boasCartasCount = 0;
        int manilhasCount = 0;

        for (TrucoCard card : intel.getCards()) {
            int cardValue = card.relativeValue(vira);
            if (cardValue == 11 || cardValue == 10) {
                manilhasCount++;
            }
            if (cardValue >= 7 && cardValue <= 9) {
                boasCartasCount++;
            }
        }
        if (manilhasCount == 1 && boasCartasCount >= 1) {
            return true;
        }
        return false;
    }


    private boolean MaoMediaSemBoasCartas(GameIntel intel) {

        TrucoCard vira = intel.getVira();

        for (TrucoCard card : intel.getCards()) {
            int cardValue = card.relativeValue(vira);
            if (cardValue < 4) {
                return false;
            }
            if (cardValue > 7) {
                return false;
            }
        }
        return true;
    }

    private boolean MaoMediaComUmaBoaCarta(GameIntel intel) {

        int boaCartaCount = 0;

        TrucoCard vira = intel.getVira();

        for (TrucoCard card : intel.getCards()) {
            int cardValue = card.relativeValue(vira);
            if (cardValue < 4) {
                return false;
            }
            if (cardValue > 7) {
                boaCartaCount++;
            }
        }
        return boaCartaCount == 1;
    }

    private boolean MaoComDuasBoasSemManilha(GameIntel intel) {

        int boaCartaCount = 0;

        TrucoCard vira = intel.getVira();

        for (TrucoCard card : intel.getCards()) {
            int cardValue = card.relativeValue(vira);
            if (cardValue >= 7 && cardValue < 10) {
                boaCartaCount++;
            }
        }
        return boaCartaCount == 2;
    }

    private boolean MaoRuimComManilha(GameIntel intel) {

        int manilhaCount = 0;

        TrucoCard vira = intel.getVira();

        for (TrucoCard card : intel.getCards()) {
            int cardValue = card.relativeValue(vira);
            if (cardValue > 4 && cardValue < 10) {
                return false;
            }
            if (cardValue >= 10) {
                manilhaCount++;
            }
        }
        return manilhaCount == 1;
    }


    private boolean MaoLixo(GameIntel intel) {

        TrucoCard vira = intel.getVira();

        for (TrucoCard card : intel.getCards()) {
            int cardValue = card.relativeValue(vira);
            if (cardValue > 5) {
                return false;
            }
        }
        return true;
    }

}
