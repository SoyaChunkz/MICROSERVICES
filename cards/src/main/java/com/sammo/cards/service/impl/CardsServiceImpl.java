package com.sammo.cards.service.impl;

import com.sammo.cards.constants.CardsConstants;
import com.sammo.cards.dto.CardsDto;
import com.sammo.cards.entity.Cards;
import com.sammo.cards.exception.CardAlreadyExistsException;
import com.sammo.cards.exception.ResourceNotFoundException;
import com.sammo.cards.mapper.CardsMapper;
import com.sammo.cards.repository.CardsRepository;
import com.sammo.cards.service.ICardsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@AllArgsConstructor
@Slf4j
public class CardsServiceImpl implements ICardsService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final CardsRepository cardsRepository;

    /**
     * @param mobileNumber - Mobile Number of the Customer
     */
    @Override
    @Transactional
    public void createCard(String mobileNumber) {
        log.info("Creating card for mobile number {}", mobileNumber);
        if (cardsRepository.findByMobileNumber(mobileNumber).isPresent()) {
            throw new CardAlreadyExistsException("Card already registered with given mobileNumber "+mobileNumber);
        }
        Cards card = cardsRepository.save(createNewCard(mobileNumber));
        log.info("Created card {} for mobile number {}", card.getCardNumber(), mobileNumber);
    }

    /**
     * @param mobileNumber - Mobile Number of the Customer
     * @return the new card details
     */
    private Cards createNewCard(String mobileNumber) {
        Cards newCard = new Cards();
        String cardNumber;
        do {
            cardNumber = Long.toString(100000000000L + RANDOM.nextLong(900000000000L));
        } while (cardsRepository.existsByCardNumber(cardNumber));
        newCard.setCardNumber(cardNumber);
        newCard.setMobileNumber(mobileNumber);
        newCard.setCardType(CardsConstants.CREDIT_CARD);
        newCard.setTotalLimit(CardsConstants.NEW_CARD_LIMIT);
        newCard.setAmountUsed(0);
        newCard.setAvailableAmount(CardsConstants.NEW_CARD_LIMIT);
        return newCard;
    }

    /**
     *
     * @param mobileNumber - Input mobile Number
     * @return Card Details based on a given mobileNumber
     */
    @Override
    public CardsDto fetchCard(String mobileNumber) {
        log.info("Fetching card for mobile number {}", mobileNumber);
        Cards cards = cardsRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Card", "mobileNumber", mobileNumber)
        );
        return CardsMapper.mapToCardsDto(cards, new CardsDto());
    }

    /**
     *
     * @param cardsDto - CardsDto Object
     * @return boolean indicating if the update of card details is successful or not
     */
    @Override
    @Transactional
    public boolean updateCard(CardsDto cardsDto) {
        log.info("Updating card {}", cardsDto.getCardNumber());
        Cards cards = cardsRepository.findByCardNumber(cardsDto.getCardNumber()).orElseThrow(
                () -> new ResourceNotFoundException("Card", "CardNumber", cardsDto.getCardNumber()));
        validateBalances(cardsDto);
        CardsMapper.mapToCards(cardsDto, cards);
        cardsRepository.save(cards);
        log.info("Updated card {}", cards.getCardNumber());
        return  true;
    }

    /**
     * @param mobileNumber - Input MobileNumber
     * @return boolean indicating if the delete of card details is successful or not
     */
    @Override
    @Transactional
    public boolean deleteCard(String mobileNumber) {
        log.info("Deleting card for mobile number {}", mobileNumber);
        Cards cards = cardsRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Card", "mobileNumber", mobileNumber)
        );
        cardsRepository.deleteById(cards.getCardId());
        log.info("Deleted card {} for mobile number {}", cards.getCardNumber(), mobileNumber);
        return true;
    }

    private void validateBalances(CardsDto cardsDto) {
        // Keep the denormalized available balance consistent with the ledger values.
        if (cardsDto.getAmountUsed() > cardsDto.getTotalLimit()
                || cardsDto.getAvailableAmount() != cardsDto.getTotalLimit() - cardsDto.getAmountUsed()) {
            throw new IllegalArgumentException("Card balances must satisfy availableAmount = totalLimit - amountUsed");
        }
    }

}
