console.log("this is script file");

const toggleSidebar=()=>{
    if($('.sidebar').is(":visible")){
$(".sidebar").css("display","none");
$(".content").css("margin-left","0%");

    }
    else{
        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");  

    }
}

/*search contacts*/

const search=()=>{
    //console.log("searching...");
	let query=$("#search-input").val();
	
	
	if(query==""){
	    $(".search-result").hide();
	}
	
	else{
	    console.log(query);
	    //sending request
	    
	    let url=`http://localhost:8080/search/${query}`;
	    
	    fetch(url).then((response)=> {
	    	return response.json();
	    }).then((data) =>{
	    	
	    	//console.log(data);
	    	
	    	let text=`<div class='list-group'>`
	    		
	    		data.forEach((contact) =>{
	        		text+=`<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'> ${contact.name}</a>`
	        		
	        		
	        	});
	        	
	    		
	    		
	    		
	    		text+=`</div>`;
	    		$(".search-result").html(text);
	    		 $(".search-result").show();
	    });
	    
	   
	}
  
};


/*first request to server to create order*/

const paymentStart=()=>{
	
console.log("payment start..");	

let amount=$("#payment_field").val();
console.log(amount);

if(amount=="" || amount==null){
	//alert("Amount is requied");
	swal("Failed!", "Amount is requied", "error");
	return;
}
//I have used ajax to send request to server to create order
$.ajax(
{
	url:'/user/create_order',
	data:JSON.stringify({amount:amount,info:'order_request'}),
	contentType:'application/json',
	type:'POST',
	dataType:'json',
	success:function(response){
		//invoked when success
		console.log(response);
		
		if(response.status=='created'){
			//open payment form
			
			let options={
				key:'rzp_test_nDsnUgItjFyszf',
				amount:response.amount,
				currency:'BDT',
				name:'Smart Contact Manager',
				description:'Donation',
				image:"https://cdn6.aptoide.com/imgs/5/b/9/5b92f9b3c2c0589cfdf37ca505ec6927_icon.png?w=160",
				order_id:response.id,
				handler:function(response){
					console.log(response.razorpay_payment_id)
					console.log(response.razorpay_order_id)
					console.log(response.razorpay_signature)
					console.log('payment successful')
					//console.log('Congrats..! payment successful')
					
 updatePaymentOnServer(response.razorpay_payment_id,response.razorpay_order_id,"paid");
							},
				
				prefill: {
			        name: "",
			        email: "",
			        contact: ""
			    },
			    notes: {
			        address: "AntTech Technology"
			    },
			    
			    theme: {
			        color: "#3399cc"
			    }
			};
			
			let rzp=new Razorpay(options);
			rzp.on('payment.failed', function (response){
		        console.log(response.error.code);
		        console.log(response.error.description);
		        console.log(response.error.source);
		        console.log(response.error.step);
		        console.log(response.error.reason);
		        console.log(response.error.metadata.order_id);
		        console.log(response.error.metadata.payment_id);
		      //  alert('OOps payment failed!!')
		        swal("Failed!", "OOps payment failed!!", "error");
		});
			rzp.open();
			
		}
	},
	error:function(error){
		//invoked when error
		
		console.log(error);
		alert("Something went wrong");
	},
});
	
};

function updatePaymentOnServer(payment_id,order_id,status){
	$.ajax(
		{
		url:'/user/update_order',
		data:JSON.stringify({payment_id:payment_id,order_id:order_id,status:status}),	
		contentType:"application/json",
		type:'POST',
		dataType:'json',
	
		success:function(response){
			swal("Good job!", "Congrats..! payment successful", "success");
		},
		error:function(error){
			 swal("Failed!", "Your payment is successful,but we didn't get on server,we will contact you as soon as possible", "error");
			
		},
	});
	
	
	
}





