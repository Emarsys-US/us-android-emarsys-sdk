package com.emarsys.predict;

import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.storage.KeyValueStore;
import com.emarsys.core.util.Assert;
import com.emarsys.predict.api.model.CartItem;
import com.emarsys.predict.util.CartItemUtils;

import java.util.List;

public class PredictInternal {

    private static final String TYPE_CART = "predict_cart";
    private static final String TYPE_ITEM_VIEW = "predict_item_view";

    private final UUIDProvider uuidProvider;
    private final TimestampProvider timestampProvider;
    private final KeyValueStore keyValueStore;
    private final RequestManager requestManager;

    public PredictInternal(KeyValueStore keyValueStore, RequestManager requestManager, UUIDProvider uuidProvider, TimestampProvider timestampProvider) {
        Assert.notNull(keyValueStore, "KeyValueStore must not be null!");
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(uuidProvider, "UuidProvider must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        this.keyValueStore = keyValueStore;
        this.requestManager = requestManager;
        this.uuidProvider = uuidProvider;
        this.timestampProvider = timestampProvider;
    }

    public void setCustomer(String customerId) {
        Assert.notNull(customerId, "CustomerId must not be null!");
        keyValueStore.putString("predict_customerId", customerId);
    }

    public String trackCart(List<CartItem> items) {
        Assert.notNull(items, "Items must not be null!");
        Assert.elementsNotNull(items, "Item elements must not be null!");

        ShardModel shard = new ShardModel.Builder(timestampProvider, uuidProvider)
                .type(TYPE_CART)
                .payloadEntry("cv", 1)
                .payloadEntry("ca", CartItemUtils.cartItemsToQueryParam(items))
                .build();

        requestManager.submit(shard);
        return shard.getId();
    }

    public String trackPurchase(String orderId, List<CartItem> items) {
        return null;
    }

    public String trackItemView(String itemId) {
        Assert.notNull(itemId, "ItemId must not be null!");

        ShardModel shard = new ShardModel.Builder(timestampProvider, uuidProvider)
                .type(TYPE_ITEM_VIEW)
                .payloadEntry("v", "i:" + itemId)
                .build();

        requestManager.submit(shard);
        return shard.getId();
    }

    public String trackCategoryView(String categoryPath) {
        return null;
    }

    public String trackSearchTerm(String term) {
        return null;
    }

}