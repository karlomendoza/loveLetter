package mx.k3m.games.loveletter.entities;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class Deck {

	public Queue<Card> cards;
	public List<Card> discard;

	public Deck() {
		List<Card> cardsList = new ArrayList<Card>();
		// TODO uncomment this
		cardsList.add(Card.GUARD);
		cardsList.add(Card.GUARD);
		cardsList.add(Card.GUARD);
		cardsList.add(Card.GUARD);
		cardsList.add(Card.GUARD);
		cardsList.add(Card.PRIEST);
		cardsList.add(Card.PRIEST);
		cardsList.add(Card.BARON);
		cardsList.add(Card.BARON);
		cardsList.add(Card.HANDMAID);
		cardsList.add(Card.HANDMAID);
		cardsList.add(Card.PRINCE);
		cardsList.add(Card.PRINCE);
		cardsList.add(Card.KING);
		cardsList.add(Card.COUNTESS);
		cardsList.add(Card.PRINCESS);

		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.PRINCESS);
		// cardsList.add(Card.PRINCESS);
		// cardsList.add(Card.PRINCESS);
		// cardsList.add(Card.PRINCESS);
		// cardsList.add(Card.PRINCESS);
		// cardsList.add(Card.PRINCESS);
		discard = new ArrayList<>();
		// cardsList.add(Card.GUARD);
		// cardsList.add(Card.GUARD);
		// cardsList.add(Card.GUARD);
		// cardsList.add(Card.GUARD);
		// cardsList.add(Card.GUARD);
		// cardsList.add(Card.GUARD);
		// cardsList.add(Card.GUARD);
		// cardsList.add(Card.GUARD);
		// cardsList.add(Card.GUARD);
		// cardsList.add(Card.GUARD);
		// cardsList.add(Card.PRIEST);
		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.PRINCE);
		// cardsList.add(Card.KING);
		// cardsList.add(Card.KING);
		// cardsList.add(Card.KING);
		// cardsList.add(Card.KING);
		// cardsList.add(Card.KING);
		// cardsList.add(Card.KING);
		// cardsList.add(Card.COUNTESS);

		Collections.shuffle(cardsList);
		cards = new ArrayDeque<Card>();
		cards.addAll(cardsList);
	}

	public Card drawCard(boolean princeFlag) {
		if (cards.size() == 1 && !princeFlag)
			return null;
		return cards.poll();
	}

	public void addToDiscard(Card card) {
		discard.add(card);
	}

}
