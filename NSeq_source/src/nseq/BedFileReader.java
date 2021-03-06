/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nseq;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

/**
 *
 * @author jssong
 */
public class BedFileReader implements DataFileReader {
    int numberChr;
    private Hashtable<String, Integer> chrLengths;
    private int Nreads = 0;
    public HashMap<String, Chromosome> chrList = new HashMap<String, Chromosome>();
    
    public BedFileReader(Hashtable<String, Integer> chrLengths, String sequencingFile){
        this.chrLengths = chrLengths;
        numberChr = chrLengths.size();
        
         for (Enumeration e = chrLengths.keys() ; e.hasMoreElements() ;) {
            String chr = (String) e.nextElement();
            
            /* add Chromosome class to each chromosome key */
            chrList.put(chr, new Chromosome( chrLengths.get(chr))) ;
         }
         
         addRawReads(sequencingFile);
    }
    
    /*
     *  Call this when the model of nuclesome centers is known.
     */
    public BedFileReader(Hashtable<String, Integer> chrLengths, String sequencingFile, int centerShift, double[] centerUncertainty){
        numberChr = chrLengths.size();
        this.chrLengths = chrLengths;
         for (Enumeration e = chrLengths.keys() ; e.hasMoreElements() ;) {
            String chr = (String) e.nextElement();
            
            /* add Chromosome class to each chromosome key */
            chrList.put(chr, new Chromosome( chrLengths.get(chr), centerShift, centerUncertainty, true)) ;
         }
         
         addRawReads(sequencingFile);
    }
    
    /*
     * Add reads and associated centers.  centerShift and centerUncertainty should be constructed before call this method
     */

    /*
     *  Just add the raw sequencing coordinates; array of nucleosome centers still need to be constructed.
     */
    private void addRawReads(String seqfilename){
        try{
            HashMap<String, PrimitiveIntegerList> posData = new HashMap<String, PrimitiveIntegerList>();
            HashMap<String, PrimitiveIntegerList> negData = new HashMap<String, PrimitiveIntegerList>();
            for( String chr: chrLengths.keySet() ){

                int L = chrLengths.get(chr).intValue();

                PrimitiveIntegerList posList = new PrimitiveIntegerList(Math.min(L/10,2000000));
                posData.put(chr, posList);

                PrimitiveIntegerList negList = new PrimitiveIntegerList(Math.min(L/10,2000000));
                negData.put(chr, negList);

            }
            
            BufferedReader f = new BufferedReader (new FileReader(seqfilename));
            String line;
            
            int start;
            int end;
            String strand;
            /** READ DATA **/
           
            while((line=f.readLine())!= null){
                if (line.trim().indexOf("#") == 0) continue;
                else{   
                    String[] columns = line.split("\t");
                    String chr = columns[0].trim();
                    if (chrLengths.containsKey(chr)){
                        Nreads ++;
                        strand = columns[5].trim();

                        if (strand.equals("+")) {

                            posData.get(chr).add(Integer.parseInt(columns[1])-1);
                        }
                        else {
                            negData.get(chr).add(Integer.parseInt(columns[2])-1);
                        }
                    }
                }
            }
                
            f.close();
            for( String chr: chrLengths.keySet() ){

              posData.get(chr).resize();
              chrList.get(chr).setPosRawData(posData.get(chr).getArray());
              //System.out.println(chr+" " + chrList.get(chr).getLength());
              negData.get(chr).resize();
              chrList.get(chr).setNegRawData(negData.get(chr).getArray());
            }
            
        } catch (IOException io){
            System.err.print("Cannot open " + seqfilename );
        }
        
    }
    
    public int getNreads(){
        return Nreads;
    }

    
    public void removeChr(String chr){
        chrList.remove(chr);
    }
    
    public HashMap<String, Chromosome> getChrList(){
        return chrList;
    }
    
    static public void main(String[] args){
        String path = "/Users/jssong/";
        Hashtable<String,Integer> chrLengths = 
                //FileParser.getChromosomeLength("/data/GenomeData/UCSC_HG18_Mar_2006/HG18_ChromosomeLength_noambiguous.txt");
                FileParser.getChromosomeLength("/Users/jssong/GSM552910_Scer.fsa_Length.txt");
        //BedFileReader br = new BedFileReader(chrLengths, path+"Combined_AcivatedNucleosome.bed", 68, NucleosomeCenter.getBetaBins(2.0, 2.0, 11));
        BedFileReader br = new BedFileReader(chrLengths, path+"GSM552910_Scer_CM_Jul2108.read.bed", 68, NucleosomeCenter.getBetaBins(2.0, 2.0, 11));
        int totPos = 0;
        int totNeg = 0;
        
        for (String chr:  br.chrList.keySet() ) {
             totPos +=((Chromosome)br.chrList.get(chr)).getPosRawList().length;
             totNeg +=((Chromosome)br.chrList.get(chr)).getNegRawList().length;
            System.out.println(chr+ "\t" + totPos);
            System.out.println(chr+ "\t" + totNeg);
            
        }
        
        
        for (String chr:  br.chrList.keySet() ) {
            int [] loc = ((Chromosome)br.chrList.get(chr)).getPosRawList();
            for (int k=0; k < loc.length; k ++){
                int tmp = loc[k]*loc[k];
            }
        }
        
        System.out.println("Total number of reads = " +br.getNreads());
        System.out.println("Postive strand = " +totPos);
        System.out.println("Negative strand = " +totNeg);
        System.out.println("Sum = " + (totPos+totNeg));
    }
    
}
