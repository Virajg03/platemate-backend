package com.platemate.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platemate.enums.ImageType;
import com.platemate.enums.RatingType;
import com.platemate.model.TiffinProvider;
import com.platemate.repository.DeliveryZoneRepository;
import com.platemate.repository.ImageRepository;
import com.platemate.repository.RatingReviewRepository;
import com.platemate.repository.TiffinProviderRepository;

@Service
public class TiffinProviderService {

    @Autowired
    private TiffinProviderRepository repository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private RatingReviewRepository ratingRepository;

    @Autowired
    private DeliveryZoneRepository deliveryZoneRepository;

    // ---------------- Basic CRUD ----------------
    public List<TiffinProvider> getAllProviders() {
        List<TiffinProvider> providers = repository.findAll();
        providers.forEach(this::loadExtras);
        return providers;
    }

    public Optional<TiffinProvider> getProviderById(Long id) {
        Optional<TiffinProvider> provider = repository.findById(id);
        provider.ifPresent(this::loadExtras);
        return provider;
    }

    public TiffinProvider createProvider(TiffinProvider provider) {
        return repository.save(provider);
    }

    public TiffinProvider updateProvider(Long id, TiffinProvider provider) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setBusinessName(provider.getBusinessName());
                    existing.setDescription(provider.getDescription());
                    existing.setCommissionRate(provider.getCommissionRate());
                    existing.setProvidesDelivery(provider.getProvidesDelivery());
                    existing.setDeliveryRadius(provider.getDeliveryRadius());
                    existing.setIsVerified(provider.getIsVerified());
                    existing.setZone(provider.getZone()); // allow updating zone
                    return repository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("TiffinProvider not found with id " + id));
    }

    public void deleteProvider(Long id) {
        repository.deleteById(id);
    }

    // ---------------- Load profile image, place images, ratings, zone ----------------
    private void loadExtras(TiffinProvider provider) {
        // Images
        provider.setProfileImage(
                imageRepository.findByImageTypeAndOwnerId(ImageType.PROFILE, provider.getTiffinProviderId())
                        .stream().findFirst().orElse(null)
        );
        provider.setPlaceImages(
                imageRepository.findByImageTypeAndOwnerId(ImageType.PLACE, provider.getTiffinProviderId())
        );

        // Ratings
        provider.setRatings(
                ratingRepository.findByRatingTypeAndTargetId(RatingType.COOK_RATING, provider.getTiffinProviderId())
        );

        // Optionally fetch zone details (already lazy loaded)
        if (provider.getZone() != null) {
            provider.setZone(deliveryZoneRepository.findById(provider.getZone().getZoneId())
                    .orElse(null));
        }
    }

    // ---------------- DeliveryZone related methods ----------------

    /**
     * Assign a DeliveryZone to a TiffinProvider
     */
    public TiffinProvider assignZone(Long providerId, Long zoneId) {
        TiffinProvider provider = repository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("TiffinProvider not found with id " + providerId));

        return deliveryZoneRepository.findById(zoneId)
                .map(zone -> {
                    provider.setZone(zone);
                    return repository.save(provider);
                })
                .orElseThrow(() -> new RuntimeException("DeliveryZone not found with id " + zoneId));
    }

    /**
     * Change the zone of an existing TiffinProvider
     */
    public TiffinProvider changeZone(Long providerId, Long newZoneId) {
        return assignZone(providerId, newZoneId);
    }
}

