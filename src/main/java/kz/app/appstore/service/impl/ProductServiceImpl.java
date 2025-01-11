package kz.app.appstore.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.app.appstore.dto.catalog.*;
import kz.app.appstore.entity.Catalog;
import kz.app.appstore.entity.Product;
import kz.app.appstore.entity.ProductImage;
import kz.app.appstore.exception.ProductCreationException;
import kz.app.appstore.repository.CatalogRepository;
import kz.app.appstore.repository.ProductRepository;
import kz.app.appstore.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CatalogRepository catalogRepository;
    private final ObjectMapper objectMapper;

    @Value("${upload.path}")
    private String uploadPath;

    public ProductServiceImpl(ProductRepository productRepository, CatalogRepository catalogRepository, ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.catalogRepository = catalogRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public CatalogResponse createCatalog(CreateCatalogRequest catalogRequest) {
        Catalog catalog = new Catalog();
        catalog.setName(catalogRequest.getName());
        catalog.setDescription(catalogRequest.getDescription());
        Catalog savedCatalog = catalogRepository.save(catalog);
        return toCatalogResponse(savedCatalog);
    }

    @Override
    public CatalogResponse createUnderCatalog(Long parentCatalogId, CreateCatalogRequest catalogRequest) {
        Catalog catalog = new Catalog();
        catalog.setName(catalogRequest.getName());
        catalog.setDescription(catalogRequest.getDescription());
        if (parentCatalogId != null) {
            Catalog parent = catalogRepository.findById(parentCatalogId)
                    .orElseThrow(() -> new RuntimeException("Родительский каталог не найден"));
            catalog.setParentCatalog(parent);
        }
        Catalog savedCatalog = catalogRepository.save(catalog);
        return toCatalogResponse(savedCatalog);
    }

    @Override
    public void deleteCatalog(Long id) {
        Catalog catalog = catalogRepository.findById(id).orElse(null);
        if (catalog != null) {
            catalogRepository.delete(catalog);
        }
    }

    @Override
    public List<CatalogResponse> getAllCatalogs() throws JsonProcessingException {
        List<Catalog> catalogs = catalogRepository.findAll();
        List<CatalogResponse> catalogResponses = catalogs.stream()
                .map(this::toCatalogResponse)
                .toList(); // Преобразуем сущности в DTO
        log.info("Got all catalogs {}", objectMapper.writeValueAsString(catalogResponses));
        return catalogResponses;
    }

    @Override
    public List<CatalogResponse> getAllCatalogsByParentId(Long parentCatalogId) throws JsonProcessingException {
        List<Catalog> catalogs = List.of();
        if (parentCatalogId != null) {
            Catalog catalog = catalogRepository.findById(parentCatalogId).orElse(null);
            if (catalog != null) {
                catalogs = catalogRepository.findByParentCatalog(catalog);
            }
            log.info("Got catalogs by parentCatalogId {}{}", catalog.getId(), objectMapper.writeValueAsString(catalogs));
        }
        return catalogs.stream()
                .map(this::toCatalogResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponseDTO createProduct(Long catalogId, CreateProductRequest request) throws ProductCreationException {
        try {
            // Валидация входных данных
            validateInput(request);
            // Получение каталога
            Catalog catalog = catalogRepository.findById(catalogId)
                    .orElseThrow(() -> new ProductCreationException("Каталог не найден"));
            // Создание товара
            Product product = new Product();
            product.setName(request.getName());
            product.setPrice(request.getPrice());
            product.setQuantity(request.getQuantity());
            product.setCatalog(catalog);

            // Обработка специфических параметров
            handleSpecificParams(product, request.getSpecificParams());

            // Обработка изображений
            List<ProductImage> productImages = handleImages(request.getImages());
            product.setImages(productImages);

            // Сохранение товара
            product = productRepository.save(product);

            // Создание и возврат DTO ответа
            return createResponseDTO(product);
        } catch (IOException e) {
            throw new ProductCreationException("Ошибка при обработке изображений товара", e);
        }
    }

    private void validateInput(CreateProductRequest request) throws ProductCreationException {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ProductCreationException("Название товара обязательно");
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new ProductCreationException("Недопустимая цена товара");
        }
        if (request.getQuantity() == null || request.getQuantity() < 0) {
            throw new ProductCreationException("Недопустимое количество товара");
        }
    }

    private void handleSpecificParams(Product product, String specificParamsJson) throws IOException {
        if (specificParamsJson != null && !specificParamsJson.trim().isEmpty()) {
            Map<String, Object> specificParams = objectMapper.readValue(specificParamsJson, Map.class);
            product.setSpecificParams(objectMapper.writeValueAsString(specificParams));
        }
    }

    private List<ProductImage> handleImages(MultipartFile[] images) throws IOException {
        List<ProductImage> productImages = new ArrayList<>();
        if (images != null) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String fileName = saveImageFile(image);
                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl("/images/" + fileName);
                    productImages.add(productImage);
                }
            }
        }
        return productImages;
    }

    private String saveImageFile(MultipartFile image) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path filePath = uploadDir.resolve(fileName);
        Files.write(filePath, image.getBytes());
        return fileName;
    }

    private ProductResponseDTO createResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        dto.setSpecificParams(product.getSpecificParams());
        dto.setImages(product.getImages().stream().map(ProductImage::getImageUrl).toList());
        dto.setCatalogId(product.getCatalog().getId());
        return dto;
    }

    @Override
    public Page<ProductResponse> getProductsByCatalogId(Long catalogId, int page, int size, String sortBy, String sortDir) {
        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new RuntimeException("Каталог не найден"));
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findByCatalog(catalog, pageable);
        return products.map(this::convertToProductResponse);
    }


    private ProductResponse convertToProductResponse(Product product) {
        // Преобразуем специфические параметры из JSON-строки в Map
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> specificParams = new HashMap<>();
        try {
            specificParams = objectMapper.readValue(product.getSpecificParams(), Map.class);
        } catch (JsonProcessingException e) {
            // Обработка исключения (можно записать в лог)
        }
        // Получаем URLs изображений
        List<String> imageUrls = product.getImages().stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());

        // Создаем и возвращаем DTO
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getQuantity(),
                specificParams,
                imageUrls
        );
    }

    public CatalogResponse toCatalogResponse(Catalog catalog) {
        ParentCatalogResponse parentCatalogResponse = null;
        if (catalog.getParentCatalog() != null) {
            parentCatalogResponse = new ParentCatalogResponse(
                    catalog.getParentCatalog().getId(),
                    catalog.getParentCatalog().getName(),
                    catalog.getParentCatalog().getDescription()
            );
        }
        return new CatalogResponse(
                catalog.getId(),
                catalog.getName(),
                catalog.getDescription(),
                parentCatalogResponse
        );
    }
}