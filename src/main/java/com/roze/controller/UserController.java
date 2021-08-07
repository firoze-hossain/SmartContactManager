package com.roze.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import com.razorpay.*;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.roze.helper.Message;
import com.roze.model.Contact;
import com.roze.model.MyOrder;
import com.roze.model.User;
import com.roze.repository.ContactRepository;
import com.roze.repository.MyOrderRepository;
import com.roze.repository.UserRepository;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder; 
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	
	@Autowired
	private MyOrderRepository myOrderRepository;
	
	
	
	// Method for adding common data
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("Username: " + userName);

		// get the user using username-email

		User user = this.userRepository.getUserByUserName(userName);

		System.out.println("USER: " + user);

		model.addAttribute("user", user);

	}

	// Dashboard Home
	@RequestMapping("/index")
	public String index(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard-Smart Contact Manager");
		return "normal/user_dashboard";

	}

	// open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {

		model.addAttribute("title", "Add Contact-Smart Contact Manager");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact_form";
	}

	// processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {
		try {

			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			// processing and uploading file

			if (file.isEmpty()) {
				System.out.println("File is empty");
				contact.setImage("default.png");
			} else {

				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is uploaded");
			}

			user.getContacts().add(contact);

			contact.setUser(user);

			this.userRepository.save(user);

			System.out.println("Data: " + contact);
			System.out.println("Added to Database");
			// success message
			session.setAttribute("message", new Message("Your contact is added!! Add more", "success"));
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();

			// error message

			session.setAttribute("message", new Message("Something went wrong!!Try again", "danger"));
		}

		return "normal/add_contact_form";
	}

	// Show contacts handler
	// per page 5 contacts=5[n]
	//current page=0[page]

	@GetMapping("/show-contacts/{page}")
	public String showContacts( @PathVariable("page")int page, Model m,Principal principal) {
		m.addAttribute("title","Contact List-Smart Contact Manager");
		
		//send the contact list to from
		
		String userName=principal.getName();
		
		User user=this.userRepository.getUserByUserName(userName);
		
		//Current page-page
		//contact per page
		Pageable pageable=  PageRequest.of(page, 5);

			Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(),pageable);
			
			m.addAttribute("contacts",contacts);
			m.addAttribute("currentPage",page);
			m.addAttribute("totalPages",contacts.getTotalPages());
		  
		 
		 
		

		return "normal/show_contacts";
	}
	
	//showing particular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId")Long cId,Model model,Principal principal ) {
		
		model.addAttribute("title","Contact Detail-Smart Contact Manager");
		System.out.println("CID: "+cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		String userName=principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
		
		model.addAttribute("contact",contact);
		model.addAttribute("title",contact.getName());
		}
		
		return "normal/contact_detail";
	

}
	
	//delete contact handler
	@GetMapping("/delete/{cId}")
	@Transactional
	public String deleteContact(@PathVariable("cId") Long cId,Model model,HttpSession session,Principal principal) {
		
		System.out.println("CID"+cId);
		
		
		Contact contact = this.contactRepository.findById(cId).get();
		
		
		System.out.println("Contact "+contact.getcId());
		
		
		User user=this.userRepository.getUserByUserName(principal.getName());
		
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		System.out.println("Deleted");
		session.setAttribute("message", new Message("Contact deleted Successfully", "success"));
		
		return "redirect:/user/show-contacts/0";
	}
	
	//open update form handler
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId")Long cId, Model m) {
		
		m.addAttribute("title","Update Contact");
		Contact contact = this.contactRepository.findById(cId).get();
		m.addAttribute("contact",contact);
		
		return "normal/update_form";
	}
	
	//update contact handler
	@RequestMapping(value = "/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,HttpSession session,Principal principal) {
		
		try {
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			//check image
			if(!file.isEmpty()) {
				//file work
				//rewrite
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldContactDetail.getImage());
				file1.delete();
				
				
				//update new photo
				
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
			}
			else {
				
				contact.setImage(oldContactDetail.getImage());
			}
			
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your contact is updated", "success"));
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Contact Name: "+contact.getName());
		System.out.println("Contact Id: "+contact.getcId());
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title","Profile");
		return "normal/profile";
		
	}
	
	//open setting handler
	
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session) {
		
		System.out.println("Old Password: "+oldPassword);
		System.out.println("New Password: "+newPassword);
		
		
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		
		System.out.println(currentUser.getPassword());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			//change password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your Pasword has been successfully changed", "success"));
			
		}
		else {
			session.setAttribute("message", new Message("Your Old Password is wrong. Please enter correct password", "danger"));
		
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
	
	//creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data,Principal principal) throws RazorpayException {
		//System.out.println("Hey order function executed");
		System.out.println(data);
		int amt= Integer.parseInt(data.get("amount").toString()) ;
		//double amt=Double.parseDouble(data.get("amount").toString());
		//float amt=Float.parseFloat(data.get("amount").toString());
		
		RazorpayClient client = new RazorpayClient("rzp_test_nDsnUgItjFyszf", "wwMAZt63hRuLQFDVIN6MXazb");
		
		JSONObject options = new JSONObject();
		options.put("amount", amt);
		options.put("currency", "BDT");
		options.put("receipt", "txn_123456");
		//Order order = razorpayClient.Orders.create(options);
		//creating new order
		
		Order order = client.Orders.create(options);
		System.out.println(order);
		
		//if you want ,you can save this to database
		MyOrder myOrder = new MyOrder();
		myOrder.setAmount(order.get("amount")+"");
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
		myOrder.setReceipt(order.get("receipt"));
		this.myOrderRepository.save(myOrder);
		
		return order.toString();
		
	}
	
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data){
		MyOrder myorder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		myorder.setPaymentId(data.get("payment_id").toString());
		myorder.setStatus(data.get("status").toString());
		this.myOrderRepository.save(myorder);
		
		System.out.println(data);
		return ResponseEntity.ok("");

	}
	
}
