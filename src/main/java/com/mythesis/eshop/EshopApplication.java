package com.mythesis.eshop;

import com.mythesis.eshop.model.entity.*;
import com.mythesis.eshop.model.repository.*;
import com.mythesis.eshop.model.service.UserService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class EshopApplication {

	private static final Logger log = LoggerFactory.getLogger(EshopApplication.class);


	public static void main(String[] args) {
		SpringApplication.run(EshopApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper(){
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setSkipNullEnabled(true);
		return modelMapper;
	}

	@Bean
	public PasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}
/*
	@Bean
	public CommandLineRunner demo(
			UserService repository,
			CategoryRepository crepo,
			ProductRepository prepo,
			OrderRepository orepo,
			OrderItemRepository oirepo,
			CartRepository ctrepo,
			CartItemRepository cirepo) {
		return (args) -> {
			// save a few customers
			User user1 = new User(
					"chris",
					"1234",
					"chris",
					"zerv",
					"larisa",
					"6999999999"
			);
			user1.setRole("ROLE_ADMIN");
			User user2 = new User(
					"tom",
					"1234",
					"tomas",
					"tin",
					"larisa",
					"6999999991"
			);
			user2.setRole("ROLE_USER");
			repository.add(user1);
			repository.add(user2);
			Category cat = new Category("Electronics");
			crepo.save(cat);

			Product product1 = new Product(
					"screen",
					"screen for you",
					"34212",
					cat,
					32.4
			);

			Product product2 = new Product(
					"tower",
					"tower for you",
					"342142",
					cat,
					10.4
			);
			Product product3 = new Product(
					"printer",
					"printer for you",
					"3131",
					cat,
					40.4
			);
			prepo.save(product1);
			prepo.save(product2);
			prepo.save(product3);

			Order order1 = new Order(
					user1,
					32.4
			);
			Order order2 = new Order(
					user2,
					32.4
			);
			orepo.save(order1);
			orepo.save(order2);
			oirepo.save(new OrderItem(
						order1,
						product1
			));
			oirepo.save(new OrderItem(
					order1,
					product2
			));
			oirepo.save(new OrderItem(
					order1,
					product3
			));
			oirepo.save(new OrderItem(
					order2,
					product3
			));
			oirepo.save(new OrderItem(
					order2,
					product2
			));

			Cart cart1 = new Cart(
					user1,
					32.0

			);


			ctrepo.save(cart1);

			cirepo.save(new CartItem(
				cart1,
				product1,
				3
			));
			cirepo.save(new CartItem(
					cart1,
					product3,
					3
			));

			// fetch all customers
			log.info("Customers found with findAll():");
			log.info("-------------------------------");
			for (User user : repository.retrieveAll()) {
				log.info(user.toString());
			}



		};
	} */

}
