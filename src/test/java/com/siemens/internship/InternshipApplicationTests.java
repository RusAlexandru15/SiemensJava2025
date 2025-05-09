package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * The InternshipApplicationTests Class uses the application-test configuration
 * It is connected to an in-memory DB called 'testdb', which is separate from the main application DB
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class InternshipApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        //reset DB before each test
        itemRepository.deleteAll();
    }


    /*------------------------------------------------------------------------------------------------
                                 TEST  Endpoint: GET /api/items
    -------------------------------------------------------------------------------------------------*/
    @Test
    void testGET_EmptyItemList() throws Exception {
        mockMvc.perform(get("/api/items")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testGET_NonEmptyItemList() throws Exception {

        List<Item> expectedItems = new java.util.ArrayList<>(List.of(
                new Item(null, "Item_1", "Desc_1", "Status_1", "a@b1.com"),
                new Item(null, "Item_2", "Desc_2", "Status_2", "a@b1.com")
        ));
        itemRepository.saveAll(expectedItems);

        ObjectMapper objectMapper = new ObjectMapper();
        String expectedItemsJson = objectMapper.writeValueAsString(expectedItems);


        mockMvc.perform(get("/api/items")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedItemsJson, false));
    }


    /*------------------------------------------------------------------------------------------------
                                 TEST  Endpoint: GET /api/items/{id}
    -------------------------------------------------------------------------------------------------*/
    @Test
    void testGET_IdFound() throws Exception {
        Item savedItem = itemRepository.save(new Item(null, "Item_1", "Desc_1", "Status_1", "a@b1.com"));

        ObjectMapper objectMapper = new ObjectMapper();
        String expectedItemJson = objectMapper.writeValueAsString(savedItem);
        Long itemId = savedItem.getId();

        mockMvc.perform(get("/api/items/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedItemJson));
    }

    @Test
    void testGET_IdNOTFound() throws Exception {

		// Intentionally use a non-existing ID to trigger 404 NOT_FOUND
		long itemWrongId = 1L;

        mockMvc.perform(get("/api/items/{id}", itemWrongId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    /*------------------------------------------------------------------------------------------------
                                 TEST  Endpoint: GET /api/items/process
    -------------------------------------------------------------------------------------------------*/
    @Test
    void testGET_ProcessEmptyItemList() throws Exception {
        mockMvc.perform(get("/api/items/process")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testGET_ProcessNonEmptyItemList() throws Exception {

        List<Item> initialItems = new java.util.ArrayList<>(List.of(
                new Item(null, "Item_1", "Desc_1", "Status_1", "a@b1.com"),
                new Item(null, "Item_2", "Desc_2", "Status_2", "a@b2.com"),
                new Item(null, "Item_3", "Desc_3", "Status_3", "a@b3.com")
        ));
        itemRepository.saveAll(initialItems);

        List<Item> expectedItems = new java.util.ArrayList<>(List.of(
                new Item(initialItems.get(0).getId(), "Item_1", "Desc_1", "PROCESSED", "a@b1.com"),
                new Item(initialItems.get(1).getId(), "Item_2", "Desc_2", "PROCESSED", "a@b2.com"),
                new Item(initialItems.get(2).getId(), "Item_3", "Desc_3", "PROCESSED", "a@b3.com")
        ));

        ObjectMapper objectMapper = new ObjectMapper();
        String expectedItemsJson = objectMapper.writeValueAsString(expectedItems);

        // First Process Test: sets all item statuses to PROCESSED and returns the processed items
        mockMvc.perform(get("/api/items/process")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedItemsJson, false));

        // Second Process Test: all items are already PROCESSED, so it returns an empty list
        mockMvc.perform(get("/api/items/process")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]", false));
    }


    /*------------------------------------------------------------------------------------------------
                             TEST  Endpoint: POST /api/items/
    -------------------------------------------------------------------------------------------------*/
    @Test
    void testPOST_CreateItemValidInput() throws Exception {

        String requestBody = """
                    {
                        "name": "Item_1",
                        "description": "Desc_1",
                        "status": "Status_1",
                        "email": "a@b1.com"
                    }
                """;

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void testPOST_CreateItemInvalidMailFormat() throws Exception {
        String requestBody = """
                    {
                        "name": "Item_1",
                        "description": "Desc_1",
                        "status": "Status_1",
                        "email": "ab1.com"
                    }
                """;

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("[\"Invalid email format\"]", false));
    }

	@Test
	void testPOST_CreateItemInvalidNameBlank() throws Exception {

		String requestBody = """
                    {
                        "name": "",
                        "description": "Desc_1",
                        "status": "Status_1",
                        "email": "a@b1.com"
                    }
                """;

		mockMvc.perform(post("/api/items")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("[\"Name must not be blank\"]", false));
	}

	@Test
	void testPOST_CreateItemInvalidBothNameAndMail() throws Exception {

		String requestBody = """
                    {
                        "name": "",
                        "description": "Desc_1",
                        "status": "12345678",
                        "email": "ab1.com"
                    }
                """;

		mockMvc.perform(post("/api/items")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("[\"Name must not be blank\",\"Invalid email format\"]", false));
	}


	/*------------------------------------------------------------------------------------------------
                             TEST  Endpoint: PUT /api/items/{id}
    -------------------------------------------------------------------------------------------------*/
	@Test
	void testPUT_UpdateItemValidInput() throws Exception {

		Item savedItem = itemRepository.save(new Item(null, "Item_1", "Desc_1", "Status_1", "a@b1.com"));

		String requestBody = """
                    {
                        "name": "Item_new",
                        "status": "Status_new"
                    }
                """;

		Item expectedItem = new Item(savedItem.getId(), "Item_new", "Desc_1", "Status_new", "a@b1.com");
		ObjectMapper objectMapper = new ObjectMapper();
		String expectedItemJson = objectMapper.writeValueAsString(expectedItem);

		mockMvc.perform(put("/api/items/{id}",savedItem.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isOk())
				.andExpect(content().json(expectedItemJson, false));
	}

	@Test
	void testPUT_UpdateItemIdNotFound() throws Exception {

		String requestBody = """
                    {
                        "name": "Item_new",
                        "status": "Status_new"
                    }
                """;

		// Intentionally use a non-existing ID to trigger 404 NOT_FOUND
		long itemWrongId = 1L;

		mockMvc.perform(put("/api/items/{id}",itemWrongId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isNotFound());
	}

	@Test
	void testPUT_UpdateItemInvalidTooLongStatus() throws Exception {

		Item savedItem = itemRepository.save(new Item(null, "Item_1", "Desc_1", "Status_1", "a@b1.com"));

		String requestBody = """
                    {
                    	"name": "Item_new",
                        "status": "New123456789012345678901"
                    }
                """;

		mockMvc.perform(put("/api/items/{id}",savedItem.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("[\"Status cannot exceed 20 characters\"]", false));
	}

	@Test
	void testPUT_UpdateItemInvalidMailFormat() throws Exception {

		Item savedItem = itemRepository.save(new Item(null, "Item_1", "Desc_1", "Status_1", "a@b1.com"));

		String requestBody = """
                    {
                        "email": "ab1.com"
                    }
                """;

		mockMvc.perform(put("/api/items/{id}",savedItem.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(content().json("[\"Invalid email format\"]", false));
	}


	/*------------------------------------------------------------------------------------------------
                             TEST  Endpoint: DELETE /api/items/{id}
    -------------------------------------------------------------------------------------------------*/
	@Test
	void testDELETE_IdFound() throws Exception {

		Item savedItem = itemRepository.save(new Item(null, "Item_1", "Desc_1", "Status_1", "a@b1.com"));

		Long itemId = savedItem.getId();

		mockMvc.perform(delete("/api/items/{id}", itemId)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	void testDELETE_IdNOTFound() throws Exception {

		// Intentionally use a non-existing ID to trigger 404 NOT_FOUND
		long itemWrongId = 1L;

		mockMvc.perform(delete("/api/items/{id}", itemWrongId)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

}
