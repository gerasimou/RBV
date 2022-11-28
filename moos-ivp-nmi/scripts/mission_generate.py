import sys

seed=int(sys.argv[1])
p_ok=float(sys.argv[2])
p_clean=float(sys.argv[3])
r_inspect=float(sys.argv[4])
r_travel=float(sys.argv[5])
r_clean=float(sys.argv[6])
r_fail_clean=float(sys.argv[7])
r_damage=float(sys.argv[8])

log=str(sys.argv[9])

# open both files
with open('charlie_template.txt','r') as firstfile, open('../missions/s3_uuvnmi/charlie.moos','w') as secondfile:
  
  # read content from first file
  for line in firstfile:     
      # append content to second file
      secondfile.write(line)

charlie_file=open('../missions/s3_uuvnmi/charlie.moos', 'a')
 
charlie_file.write('//------------------------------------------\n')
charlie_file.write('// sUUV config block\n')
charlie_file.write('ProcessConfig = sUUVNMI\n')
charlie_file.write('{\n')
charlie_file.write('  max_appcast_events = 50\n')
charlie_file.write('  PORT                = 8860\n')
charlie_file.write('\n')
charlie_file.write('  SEED                = '+str(seed)+'\n')
charlie_file.write('\n')
charlie_file.write('  P_CHAIN_OK          = '+str(p_ok)+'\n')
charlie_file.write('  P_CLEAN_CHAIN       = '+str(p_clean)+'\n')
charlie_file.write('  R_CHAIN_INSPECT     = '+str(r_inspect)+'\n')
charlie_file.write('  R_CHAIN_TRAVEL      = '+str(r_travel)+'\n')
charlie_file.write('  R_CHAIN_CLEAN       = '+str(r_clean)+'\n')
charlie_file.write('  R_CHAIN_FAIL_CLEAN  = '+str(r_fail_clean)+'\n')
charlie_file.write('  R_DAMAGE            = '+str(r_damage)+'\n')
charlie_file.write('\n')
charlie_file.write('  LOG                 = '+log+'\n')
charlie_file.write('}')