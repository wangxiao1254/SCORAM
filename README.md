# SCORAM: Oblivious RAM for Secure Computation
to be appeared on *21st ACM Conference on Computer and Communications Security 2014*. [Link to paper](http://eprint.iacr.org/2014/671)

_**We later designed a new ORAM called [Circuit ORAM](http://eprint.iacr.org/2014/672) with the smallest circuit size both asymptotically and in practice**._

## Authors

[Xiao Shaun Wang](http://www.cs.umd.edu/~wangxiao/) (University of Maryland, College Park)

[Yan Huang](http://yhuangpress.wordpress.com/) (Indiana University, Bloomington)

[T-H. Hubert Chan](http://i.cs.hku.hk/~hubert/) (University of Hong Kong)

[abhi shelat](http://www.cs.virginia.edu/~shelat/Virginia.html) (University of Virginia)

[Elaine Shi](http://www.cs.umd.edu/~elaine/) (University of Maryland, College Park)



## Step by step instructions to run the code:

1. open a terminal. (followings are commands in terminal)
2. `git clone git@github.com:wangxiao1254/SCORAM.git`
3. `cd SCORAM`
4. `./compile.sh`
5. `java  -cp bin:lib/* test.VerifySCORAM 12345 7 6 32 8 5 200 200` 
 to test the correctness of SCORAM construction,
detailed explanation of parameters can be found by `java  -cp bin:lib/* test.VerifySCORAM`
6. open another terminal and run `java -cp bin:lib/* test.TestSCORAMClient localhost 12345` on one terminal and
`java  -cp bin:lib/* test.TestSCORAMServer 12345 20 6 32  8 10` on the other terminal. detail explanation
of parameters can be found by `java  -cp bin:lib/* test.TestSCORAMServer`

## Point of contact:

1. Xiao Shaun Wang (wangxiao@cs.umd.edu)
2. Yan Huang (yhuang@cs.umd.edu)

