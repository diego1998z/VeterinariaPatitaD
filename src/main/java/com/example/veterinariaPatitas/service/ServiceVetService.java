package com.example.veterinariaPatitas.service;

import com.example.veterinariaPatitas.model.ServiceVet;
import com.example.veterinariaPatitas.repository.ServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceVetService {

    private final ServiceRepository serviceRepository;

    public ServiceVetService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<ServiceVet> findAll() {
        return serviceRepository.findAll();
    }

    public Optional<ServiceVet> findById(Integer id) {
        if (id == null) return Optional.empty();
        return serviceRepository.findById(id);
    }

    public ServiceVet save(ServiceVet serviceVet) {
        if (serviceVet == null) return null;
        return serviceRepository.save(serviceVet);
    }

    public void delete(Integer id) {
        if (id == null) return;
        serviceRepository.deleteById(id);
    }
}
