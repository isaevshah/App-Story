package kz.app.appstore.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import kz.app.appstore.dto.catalog.*;
import kz.app.appstore.dto.product.CreateProductRequest;
import kz.app.appstore.dto.product.ProductResponse;
import kz.app.appstore.dto.product.ProductResponseDTO;
import kz.app.appstore.dto.product.UpdateProductRequest;
import kz.app.appstore.entity.Catalog;
import kz.app.appstore.entity.Product;
import kz.app.appstore.entity.ProductImage;
import kz.app.appstore.exception.ProductCreationException;
import kz.app.appstore.repository.CatalogRepository;
import kz.app.appstore.repository.ProductRepository;
import kz.app.appstore.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.Retryable;
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
    public List<CatalogResponse> getAllCatalogsByParentId(Long parentCatalogId) {
        if (parentCatalogId == null) {
            throw new IllegalArgumentException("Parent catalog ID cannot be null");
        }
        if (!catalogRepository.existsById(parentCatalogId)) {
            throw new RuntimeException("Parent catalog not found with ID: " + parentCatalogId);
        }
        List<Catalog> catalogs = catalogRepository.findByParentCatalogId(parentCatalogId);
        return catalogs.stream()
                .map(this::toCatalogResponse)
                .toList();
    }


    @Override
    @Transactional
    @Retryable(value = DataIntegrityViolationException.class, maxAttempts = 5)
    public ProductResponseDTO createProduct(Long catalogId, CreateProductRequest request) throws ProductCreationException {
        try {
            validateInput(request);
            Catalog catalog = catalogRepository.findById(catalogId)
                    .orElseThrow(() -> new ProductCreationException("Каталог не найден"));
            Product product = new Product();
            product.setName(request.getName());
            product.setPrice(request.getPrice());
            product.setQuantity(request.getQuantity());
            product.setCatalog(catalog);

            // Генерация и установка уникального кода
            String individualCode = generateUniqueCode();
            product.setIndividualCode(individualCode);

            handleSpecificParams(product, request.getSpecificParams());
            List<ProductImage> productImages = handleImages(request.getImages(), product);

            product.setImages(productImages);
            product = productRepository.save(product);
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
            Map<String, String> specificParams = objectMapper.readValue(specificParamsJson, Map.class);
            product.setSpecificParams(objectMapper.writeValueAsString(specificParams));
        } else {
            product.setSpecificParams(null);
        }
    }



    private List<ProductImage> handleImages(MultipartFile[] images, Product product) throws IOException {
        List<ProductImage> productImages = new ArrayList<>();
        if (images != null) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String fileName = saveImageFile(image);
                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl("/images/" + fileName);
                    productImages.add(productImage);
                    productImage.setProduct(product);
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
        if (!catalogRepository.existsById(catalogId)) {
            throw new RuntimeException("Каталог не найден");
        }
        List<String> allowedSortFields = Arrays.asList("id", "name", "price", "quantity");
        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "id"; // Поле сортировки по умолчанию
        }
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findByCatalogId(catalogId, pageable);
        return products.map(this::convertToProductResponse);
    }

    @Override
    public List<ProductResponse> getLikedProducts() {
        return List.of();
    }

    @Override
    public Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) throws JsonProcessingException {
        List<String> allowedSortFields = Arrays.asList("id", "name", "price", "quantity");
        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "id"; // Поле сортировки по умолчанию
        }
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.getAllProducts(pageable);
        return products.map(this::convertToProductResponse);
    }

    @Override
    public void updateProduct(Long productId, UpdateProductRequest request) throws ProductCreationException {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

            product.setName(request.getName());
            product.setPrice(request.getPrice());
            product.setQuantity(request.getQuantity());

            handleSpecificParams(product, request.getSpecificParams());
            updateProductImages(product, request.getImages());

            productRepository.save(product);
        } catch (IOException e) {
            throw new ProductCreationException("Ошибка при обработке изображений товара", e);
        }
    }

    @Override
    public ProductResponse getProductDetails(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
        return convertToProductResponse(product);
    }

    @Override
    public void updateCatalog(Long catalogId, CreateCatalogRequest catalogRequest) {
        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + catalogId));
        catalog.setName(catalogRequest.getName());
        catalog.setDescription(catalogRequest.getDescription());
        catalogRepository.save(catalog);
    }

    private ProductResponse convertToProductResponse(Product product) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> specificParams = new HashMap<>();
        try {
            specificParams = objectMapper.readValue(product.getSpecificParams(), Map.class);
        } catch (JsonProcessingException e) {
            log.error("Ошибка при разборе JSON для specificParams у продукта с ID: {}", product.getId(), e);
        }
        List<String> imageUrls = product.getImages().stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());

        return new ProductResponse(
                product.getId(),
                product.getCatalog().getName(),
                product.getIndividualCode(),
                product.getName(),
                product.getPrice(),
                product.getQuantity(),
                product.getDescription(),
                product.getLiked(),
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

    private String generateUniqueCode() {
        long timestamp = System.currentTimeMillis();
        Random random = new Random();
        String timestampStr = Long.toString(timestamp);
        StringBuilder code = new StringBuilder(timestampStr.substring(timestampStr.length() - 8));
        while (code.length() < 8) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private void updateProductImages(Product product, MultipartFile[] newImageFiles) throws IOException {
        List<ProductImage> imagesToRemove = new ArrayList<>(product.getImages());
        if (newImageFiles != null) {
            for (MultipartFile image : newImageFiles) {
                if (!image.isEmpty()) {
                    String fileName = saveImageFile(image);
                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl("/images/" + fileName);
                    productImage.setProduct(product);
                    product.getImages().add(productImage);
                }
            }
        }
        // Удаляем старые изображения, которых нет в новом наборе
        product.getImages().removeAll(imagesToRemove);
        for (ProductImage imageToRemove : imagesToRemove) {
            imageToRemove.setProduct(null);
        }
    }
}