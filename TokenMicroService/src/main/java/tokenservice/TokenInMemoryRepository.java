package tokenservice;

import java.util.*;

public class TokenInMemoryRepository implements ITokenRepository{
    Map<UUID, String> tokenCustomerMap = new HashMap<>();


    @Override
    public void add(UUID token, String customerId) {
        System.out.println("add cust id" + customerId);
        tokenCustomerMap.put(UUID.randomUUID(), customerId);
    }

    @Override
    public List<UUID> getTokensForCustomer(String customerId) {
        List<UUID> tokens = new ArrayList<>();
        for (UUID uuid : tokenCustomerMap.keySet()) {
            System.out.println("uuid" + uuid);
            System.out.println("customerId " + customerId);
            System.out.println(tokenCustomerMap.get(uuid));

            if (tokenCustomerMap.get(uuid).equals(customerId)) tokens.add(uuid);
        }
        return tokens;
    }

    @Override
    public void deleteTokensForCustomer(String customerId) {
        tokenCustomerMap.entrySet().removeIf(entry -> entry.getValue().equals(customerId));
    }

    @Override
    public String consumeToken(UUID customerToken) {
        String customerId = tokenCustomerMap.remove(customerToken);
        return customerId;
    }
}
