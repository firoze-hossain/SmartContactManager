
package com.roze.repository;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.roze.model.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {

	// pagination
//Current page-page
	//contact per page
	@Query("from Contact as c where c.user.id = :userId")
	public Page<Contact> findContactsByUser(@Param("userId")Long userId,Pageable pageable);
	
	
}
