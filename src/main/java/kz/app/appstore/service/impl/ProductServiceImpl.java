package kz.app.appstore.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.app.appstore.dto.catalog.CreateCatalogRequest;
import kz.app.appstore.entity.Catalog;
import kz.app.appstore.repository.CatalogRepository;
import kz.app.appstore.repository.ProductRepository;
import kz.app.appstore.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CatalogRepository catalogRepository;
    private final ObjectMapper objectMapper;

    public ProductServiceImpl(ProductRepository productRepository, CatalogRepository catalogRepository, ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.catalogRepository = catalogRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Catalog createCatalog(CreateCatalogRequest catalogRequest) {
        Catalog catalog = new Catalog();
        catalog.setName(catalogRequest.getName());
        catalog.setDescription(catalogRequest.getDescription());
        return catalogRepository.save(catalog);
    }

    @Override
    public Catalog createUnderCatalog(Long parentCatalogId, CreateCatalogRequest catalogRequest) {
        Catalog catalog = new Catalog();
        catalog.setName(catalogRequest.getName());
        catalog.setDescription(catalogRequest.getDescription());
        if (parentCatalogId != null) {
            Catalog parent = catalogRepository.findById(parentCatalogId)
                    .orElseThrow(() -> new RuntimeException("Родительский каталог не найден"));
            catalog.setParentCatalog(parent);
        }
        return catalogRepository.save(catalog);
    }

    @Override
    public void deleteCatalog(Long id) {
        Catalog catalog = catalogRepository.findById(id).orElse(null);
        if (catalog != null) {
            catalogRepository.delete(catalog);
        }
    }

    @Override
    public List<Catalog> getAllCatalogs() throws JsonProcessingException {
        List<Catalog> catalogs = catalogRepository.findAll();
        log.info(" Got all catalogs {}", objectMapper.writeValueAsString(catalogs));
        return catalogs;
    }

    @Override
    public List<Catalog> getAllCatalogsByParentId(Long parentCatalogId) throws JsonProcessingException {
        List<Catalog> catalogs = List.of();
        if (parentCatalogId != null) {
            Catalog catalog = catalogRepository.findById(parentCatalogId).orElse(null);
            if (catalog != null) {
                catalogs = catalogRepository.findByParentCatalog(catalog);
            }
            log.info("Got catalogs by parentCatalogId {}{}", catalog.getId(), objectMapper.writeValueAsString(catalogs));
        }
        return catalogs;
    }
}