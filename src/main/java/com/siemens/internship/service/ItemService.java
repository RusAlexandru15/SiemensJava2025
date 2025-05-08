package com.siemens.internship.service;

import com.siemens.internship.dto.ItemCreateDTO;
import com.siemens.internship.dto.ItemUpdateDTO;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    private  List<Item> processedItems = new CopyOnWriteArrayList<>();
    private  AtomicInteger processedCount = new AtomicInteger(0);;


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(ItemCreateDTO dto) {
        Item item = new Item();

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setStatus(dto.getStatus());
        item.setEmail(dto.getEmail());
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    /**
     * Updates an existing item
     * Skips any fields that are null or blank (only new description can be blank)
     * Throws an exception if the item is not found
     */
    public Item updateItem(Long id, ItemUpdateDTO dto) {
        Item existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            existingItem.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            existingItem.setDescription(dto.getDescription());
        }

        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            existingItem.setStatus(dto.getStatus());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            existingItem.setEmail(dto.getEmail());
        }

        return itemRepository.save(existingItem);
    }



    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */


    /**
     * My Solution
     * 1) What was wrong with the original implementation: (each issue has its fixe below)
     *      - @Async was misused (method returned synchronously list<>, not a CompletableFuture )
     *      - Used non-thread-safe shared resources (list and counter)
     *      - Thread.sleep() caused inefficient resource use
     *      - All items were processed even if already marked as "PROCESSED"
     *      - Exceptions inside async tasks were not properly handled (only InterruptedException was caught)
     *      - Returned the result before all async tasks completed
     *
     * 2) Fixes
     *      - Changed return type to CompletableFuture<List<Item>> to properly support async execution
     *      - Used CopyOnWriteArrayList for processedItems and AtomicInteger for processedCount
     *      - Removed Thread.sleep()
     *      - Added DB query to load only UNPROCESSED items (not ids) and reduced repository access from 3 calls to 2
     *      - Logged  all exceptions (not just InterruptedException)
     *      - Returned the result after all async tasks completed using allOf method
     *      */

    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        List<Item> unprocessedItems = itemRepository.findAllUnprocessedItems();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Reset processed items and count at every call
        processedItems = new CopyOnWriteArrayList<>();
        processedCount = new AtomicInteger(0);

        for (Item item : unprocessedItems) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {

                    item.setStatus("PROCESSED");
                    itemRepository.save(item);
                    processedItems.add(item);
                    processedCount.incrementAndGet();

                } catch (Exception e) {
                    System.err.println("Error processing item " + item.getId() + ": " + e.getMessage());
                }
            }, executor);
            futures.add(future);
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> processedItems);
    }

}

