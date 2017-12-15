package mx.k3m.games.loveletter.entities;

import java.util.ArrayList;
import java.util.List;

public class Hand {

	private List<Card> cards;

	public Hand() {
		cards = new ArrayList<Card>();
	}

	public Hand(Card card) {
		cards = new ArrayList<Card>();
		cards.add(card);
	}

	public List<Card> getCards() {
		return cards;
	}

	public void setCards(List<Card> cards) {
		this.cards = cards;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Card card : cards) {
			sb.append(card.name());
		}
		return sb.toString();
	}
}
