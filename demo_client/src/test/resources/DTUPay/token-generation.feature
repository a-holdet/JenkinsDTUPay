Feature: TokenGeneration
	Scenario: Successful Token Generation
		Given the customer "Stein" "Bagger" with CPR "290276-7777" has a bank account
		And the customer is registered with DTUPay
		And the customer has 0 tokens
		When the customer requests 5 tokens
		Then the customer has 5 tokens

	Scenario: Customer has no Bank Account
		Given the customer with name "Stein" "Bagger" and CPR "290276-1211" has no bank account
		When the customer requests 5 tokens
		Then the token granting is not successful
		And the received error message is "Customer must have a customer id to request tokens"

	Scenario: Customer has multiple tokens and requests more tokens
		Given the customer "Stein" "Bagger" with CPR "290276-7777" has a bank account
		And the customer is registered with DTUPay
		And the customer has 0 tokens
		And the customer requests 2 tokens
		And the customer has 2 tokens
		When the customer requests 2 tokens
		Then the token granting is denied
		And the error message is "Customer cannot request more tokens"