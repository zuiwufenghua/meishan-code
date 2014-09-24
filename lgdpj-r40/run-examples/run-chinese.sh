nohup ../lgdpj-r config-chinese.txt >> train.log 2>&1 &
#nohup ../lgdpj-r config-chinese.txt --train=0 --test=1 --param-num-for-eval=7 --thread-num=16 --inst-max-len-to-throw=500 >> test.log 2>&1 &