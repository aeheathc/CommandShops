package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

import cuboidLocale.BookmarkedResult;

public class PlayerData {
	// Objects
	private LocalShops plugin = null;
	
	// Attributes
	protected List<String> shopList = Collections.synchronizedList(new ArrayList<String>());
	protected BookmarkedResult bookmark = new BookmarkedResult();
	protected String playerName = null;
	protected boolean isSelecting = false;
	protected boolean sizeOkay = false;
	private long xyzA[] = null;
	private long xyzB[] = null;
	protected String size = "";
	
	// Constructor
	public PlayerData(LocalShops plugin, String playerName) {
		this.plugin = plugin;
		this.playerName = playerName;
	}
	
	public long[] getPositionA() {
		return xyzA;
	}
	
	public long[] getPositionB() {
		return xyzB;
	}
	
	public void setPositionA(long[] xyz) {
		xyzA = xyz.clone();
		checkSize(xyzA, xyzB);
	}
	
	public void setPositionB(long[] xyz) {
		xyzB = xyz.clone();
		checkSize(xyzA, xyzB);
	}
	
	public String getSizeString() {
		return size;
	}
	
	private boolean checkSize(long[] xyzA, long[] xyzB) {
		if(xyzA != null && xyzB != null) {
			long width1 = Math.abs(xyzA[0] - xyzB[0]) + 1;
			long height = Math.abs(xyzA[1] - xyzB[1]) + 1;
			long width2 = Math.abs(xyzA[2] - xyzB[2]) + 1;
			
			size = "" + width1 + "x" + height + "x" + width2;
			
			if( width1 > plugin.shopData.maxWidth || width2 > plugin.shopData.maxWidth || height > plugin.shopData.maxHeight ) {
				return false;
			} else {
				return true;
			}
		}
		return false;
		
	}
	
	public boolean addPlayerToShop( String shopName ) {
		String playerWorld = plugin.getServer().getPlayer(playerName).getWorld().getName();
		
		if( !playerIsInShop( shopName ) && plugin.shopData.getShop(shopName).getWorldName().equalsIgnoreCase(playerWorld)) {
			shopList.add(shopName);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean playerIsInShop( String shopName ) {
		String playerWorld = plugin.getServer().getPlayer(playerName).getWorld().getName();
				
		if( shopList.contains(shopName) ){
			if(	plugin.shopData.getShop(shopName).getWorldName().equalsIgnoreCase(playerWorld)) {
				return true;
			}
		}
		return false;
	}

	public void removePlayerFromShop(Player player, String shopName) {
		shopList.remove(shopName);
	}

	public List<String> playerShopsList(String playerName) {
		return shopList;
	}

	public boolean payPlayer(String playerName, int cost) {
		if( plugin.pluginListener.useiConomy ) {
			iConomy ic = plugin.pluginListener.iConomy;
			Account account = ic.getBank().getAccount(playerName);
			if(account == null) {
				ic.getBank().addAccount(playerName);
				account = ic.getBank().getAccount(playerName);
			}
			double balance = account.getBalance();
			account.setBalance(balance + (double)cost);
			plugin.shopData.logPayment(playerName, "payment", cost, balance, balance + (double)cost);
			return true; 
		}
		return false;
	}

	public boolean payPlayer(String playerFrom, String playerTo, int cost) {
		if( plugin.pluginListener.useiConomy ) {
			iConomy ic = plugin.pluginListener.iConomy;
			
			Account accountFrom = ic.getBank().getAccount(playerFrom);
			if(accountFrom == null) {
				ic.getBank().addAccount(playerFrom);
				accountFrom = ic.getBank().getAccount(playerFrom);
			}
			double balanceFrom = accountFrom.getBalance();
			
			Account accountTo = ic.getBank().getAccount(playerTo);
			if(accountTo == null) {
				ic.getBank().addAccount(playerTo);
				accountTo = ic.getBank().getAccount(playerTo);
			}
			double balanceTo = accountTo.getBalance();
			
			if( balanceFrom < cost ) return false;

			
			accountFrom.setBalance(balanceFrom - cost);
			plugin.shopData.logPayment(playerFrom, "payment", cost, balanceFrom, balanceFrom + cost);
			accountTo.setBalance(balanceTo + cost);
			plugin.shopData.logPayment(playerTo, "payment", cost, balanceTo, balanceTo + cost);
			return true; 
		}
		return false;
	}

	public long getBalance(String shopOwner) {
		if( plugin.pluginListener.useiConomy ) {
			iConomy ic = plugin.pluginListener.iConomy;
			
			Account account = ic.getBank().getAccount(shopOwner);
			if(account == null) {
				ic.getBank().addAccount(shopOwner);
				account = ic.getBank().getAccount(shopOwner);
			}
			double balanceFrom = account.getBalance();
			
			return (long)Math.floor(balanceFrom);
		}
		return 0;
	}

	public boolean chargePlayer(String shopOwner, long chargeAmount) {
		if( plugin.pluginListener.useiConomy ) {
			iConomy ic = plugin.pluginListener.iConomy;
			if(ic == null) return false;
			
			Account account = ic.getBank().getAccount(shopOwner);
			if(account == null) {
				ic.getBank().addAccount(shopOwner);
				account = ic.getBank().getAccount(shopOwner);
			}
			double balanceFrom = account.getBalance();
			double newBalance = balanceFrom - chargeAmount;
			if(balanceFrom >= chargeAmount) {
				account.setBalance(newBalance);
				plugin.shopData.logPayment(shopOwner, "payment", chargeAmount, balanceFrom, newBalance);
				return true;
			} else {
				return false;
			}
			
		}
		return false;
	}

}
