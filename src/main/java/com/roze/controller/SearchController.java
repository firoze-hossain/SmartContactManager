package com.roze.controller;

import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.roze.model.Contact;
import com.roze.model.User;
import com.roze.repository.ContactRepository;
import com.roze.repository.UserRepository;  


@RestController
public class SearchController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	//search handler
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query,Principal principal){
		System.out.println(query);
		User user=this.userRepository.getUserByUserName(principal.getName());
		
		List<Contact> contacts = this.contactRepository.findByNameContainingAndUser(query, user);
		
		return ResponseEntity.ok(contacts);
	}

}
