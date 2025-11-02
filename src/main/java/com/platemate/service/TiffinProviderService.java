package com.platemate.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.platemate.dto.TiffinProviderRequest;
import com.platemate.enums.ImageType;
import com.platemate.enums.RatingType;
import com.platemate.model.DeliveryZone;
import com.platemate.model.Image;
import com.platemate.model.TiffinProvider;
import com.platemate.model.User;
import com.platemate.repository.DeliveryZoneRepository;
import com.platemate.repository.ImageRepository;
import com.platemate.repository.RatingReviewRepository;
import com.platemate.repository.TiffinProviderRepository;
import com.platemate.repository.UserRepository;

@Service
public class TiffinProviderService {

    @Autowired
    private TiffinProviderRepository repository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private RatingReviewRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeliveryZoneRepository deliveryZoneRepository;

    // ---------------- Basic CRUD ----------------
    public List<TiffinProvider> getAllProviders() {
        System.err.println("=============in getAllProvider==========");
        List<TiffinProvider> providers = repository.findAll();
        providers.forEach(this::loadExtras);
        return providers;
    }

    public Optional<TiffinProvider> getProviderById(Long id) {
        Optional<TiffinProvider> provider = repository.findById(id);
        provider.ifPresent(this::loadExtras);
        return provider;
    }

    public TiffinProvider createProvider(TiffinProviderRequest request) {
        TiffinProvider provider = new TiffinProvider();

        // ✅ Fetch related entities safely
        User user = userRepository.findById(request.getUser())
                .orElseThrow(() -> new RuntimeException("User not found with id " + request.getUser()));

        DeliveryZone zone = deliveryZoneRepository.findById(request.getZone())
                .orElseThrow(() -> new RuntimeException("Zone not found with id " + request.getZone()));

        // ✅ Set data
        provider.setUser(user);
        provider.setZone(zone);
        provider.setBusinessName(request.getBusinessName());
        provider.setDescription(request.getDescription());
        provider.setCommissionRate(request.getCommissionRate());
        provider.setProvidesDelivery(request.getProvidesDelivery());
        provider.setDeliveryRadius(request.getDeliveryRadius());
        provider.setIsVerified(request.getIsVerified());

        // ✅ Save
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
    @Transactional(readOnly = true)
    private void loadExtras(TiffinProvider provider) {
        System.out.println("===========In Load Extras===Business==name===" + provider.getBusinessName());
        System.out.println("===========In Load Extras===Description=====" + provider.getDescription());
        System.out.println("===========In Load Extras===getTiffinProviderId====" + provider.getTiffinProviderId());

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

