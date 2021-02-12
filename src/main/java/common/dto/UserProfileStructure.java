package common.dto;

import lombok.Data;
import server.domain.BackpackItem;
import server.domain.InventoryItem;

@Data
public class UserProfileStructure {

    public int id;

    public String name;

    public int level;

    public int experience;

    public int energy;

    public int rating;

    public int money;

    public BackpackItem[] backpack;

    public InventoryItem[] inventory;

    public int[] friends;
}
