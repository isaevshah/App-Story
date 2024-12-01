package kz.app.appstore.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.app.appstore.dto.catalog.CreateCatalogRequest;
import kz.app.appstore.dto.catalog.CreateProductRequest;
import kz.app.appstore.dto.catalog.ProductResponse;
import kz.app.appstore.entity.Catalog;
import kz.app.appstore.entity.Product;
import kz.app.appstore.entity.ProductImage;
import kz.app.appstore.repository.CatalogRepository;
import kz.app.appstore.repository.ProductRepository;
import kz.app.appstore.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public Product createProduct(Long catalogId, CreateProductRequest request) throws IOException {
        // Извлекаем общие параметры продукта
        String name = request.getName();
        Double price = request.getPrice();
        Long quantity = request.getQuantity();

        // Получаем каталог из базы данных
        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new RuntimeException("Каталог не найден"));

        // Создаем новый объект продукта
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setCatalog(catalog);

        // Преобразуем специфические параметры из JSON-строки в Map
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> specificParams = objectMapper.readValue(request.getSpecificParams(), Map.class);

        // Сохраняем специфические параметры в продукте
        String specificParamsJson = objectMapper.writeValueAsString(specificParams);
        product.setSpecificParams(specificParamsJson);

        // Обрабатываем и сохраняем изображения
        MultipartFile[] images = request.getImages();
        List<ProductImage> productImages = new ArrayList<>();
        if (images != null) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    // Сохраняем файл на сервере
                    String fileName = saveImageFile(image);

                    // Создаем объект ProductImage
                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl("/images/" + fileName);
                    productImage.setProduct(product);

                    productImages.add(productImage);
                }
            }
        }

        // Добавляем изображения к продукту
        product.setImages(productImages);

        // Сохраняем продукт и связанные изображения в базе данных
        productRepository.save(product);

        return product;
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


    private String saveImageFile(MultipartFile image) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        String uploadDir = "/path/to/upload/directory/";
        // Создаем директорию, если она не существует
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        // Сохраняем файл
        Path filePath = Paths.get(uploadDir, fileName);
        Files.write(filePath, image.getBytes());

        return fileName;
    }



}